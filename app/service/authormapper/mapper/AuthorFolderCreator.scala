package service.authormapper.mapper

import components.publishone.{FolderApi, NodeOperationApi}
import play.api.Logger
import service.authormapper.cache.PublishOneCache
import service.authormapper.model.{Author, AuthorFolder}
import util.PublishOneConstants._
import util.StringUtils._

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Creates PublishOne folder which should contain author details
  *
  * @param folderApi PublishOne Folder API
  * @param publishOneCache PublishOne cache
  */
class AuthorFolderCreator @Inject()(folderApi: FolderApi, nodeOperationApi: NodeOperationApi, publishOneCache: PublishOneCache) {

  private lazy val log = Logger(getClass)

  def create(author: Author): Future[AuthorFolder] = {
    log.info(s"$author Creating author folder ...")
    for {
      parentFolderId <- getOrCreateTwoLettersAuthorFolder(author.familyName)
      folder <- createAuthorFolder(parentFolderId, author)
      _ <- Future.successful(log.info(s"$author Author folder created $folder"))
    } yield folder
  }

  private def getOrCreateTwoLettersAuthorFolder(familyName: String): Future[Int] = {
    val twoLettersFolderName = familyName.substring(0, 2).toLowerCase
    val twoLettersFolderId = publishOneCache.rootFoldersCache.get(twoLettersFolderName)
    if (twoLettersFolderId.isEmpty) createTwoLettersAuthorFolder(familyName, twoLettersFolderName.capitalize)
    else Future.successful(twoLettersFolderId.get)
  }

  private def createTwoLettersAuthorFolder(familyName: String, twoLettersFolderName: String) = {
    log.info(s"Creating two letters author folder $twoLettersFolderName ...")
    val oneLettersFolderName = familyName.substring(0, 1).toLowerCase
    val oneLettersFolderId = publishOneCache.rootFoldersCache.get(oneLettersFolderName)
    if (oneLettersFolderId.isEmpty) throw new RuntimeException(s"There is no author folder $oneLettersFolderName")
    else
      folderApi
        .createFolder(oneLettersFolderId.get, twoLettersFolderName, documentTypeAuthor)
        .map { response =>
          val folderId = (response \ "id").as[Int]
          val cachedFolderId = publishOneCache.rootFoldersCache.putIfAbsent(twoLettersFolderName.toLowerCase, folderId).getOrElse(folderId)
          if (folderId != cachedFolderId) nodeOperationApi.deleteNode(folderId, includeDescendants = true)
          cachedFolderId
        }
  }

  private def createAuthorFolder(parentFolderId: Int, author: Author) = {
    val folderName = buildFolderName(author)
    val metadata = buildFolderMetadata(author)
    folderApi
      .createFolder(parentFolderId, folderName, documentTypeAuthor, metadata)
      .map { resp =>
        val id = (resp \ "id").as[Int]
        val name = (resp \ "name").as[String]
        AuthorFolder(id, name)
      }
  }

  private def buildFolderName(author: Author) = {
    val folderName = new StringBuilder(author.familyName)
    if (notEmpty(author.initials) || notEmpty(author.familyNamePrefix)) {
      folderName ++= s","
    }
    if (notEmpty(author.initials)) {
      folderName ++= s" ${author.initials}"
    }
    if (notEmpty(author.familyNamePrefix)) {
      folderName ++= s" ${author.familyNamePrefix}"
    }
    folderName.toString
  }

  private def buildFolderMetadata(author: Author) = {
    Map(
      "givenName" -> author.givenName,
      "familyName" -> author.familyName,
      "initials" -> author.initials,
      "gender" -> publishOneCache.mapListItemValue(listItemsGender, author.gender),
      "prefix" -> publishOneCache.mapListItemValue(listItemsPrefix, author.prefix),
      "familyNamePrefix" -> publishOneCache.mapListItemValue(listItemsFamilyNamePrefix, author.familyNamePrefix)
    )
  }

}
