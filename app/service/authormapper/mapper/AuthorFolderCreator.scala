package service.authormapper.mapper

import components.publishone.{FolderApi, NodeOperationApi}
import play.api.Logger
import play.api.libs.json.JsValue
import service.authormapper.cache.AuthorRootFoldersCache
import service.authormapper.model.{Author, AuthorFolder}
import service.common.cache.ValueListCache
import util.CreationStatus
import util.PublishOneConstants.{documentTypeAuthor, _}
import util.StringUtils._

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Creates PublishOne folder which should contain author details
  *
  * @param folderApi PublishOne Folder API
  * @param nodeOperationApi PublishOne Node operation API
  * @param valueListCache PublishOne value list items cache
  * @param authorRootFoldersCache PublishOne author root folders cache
  */
class AuthorFolderCreator @Inject()(folderApi: FolderApi,
                                    nodeOperationApi: NodeOperationApi,
                                    valueListCache: ValueListCache,
                                    authorRootFoldersCache: AuthorRootFoldersCache,
                                    authorListItemsHandler: AuthorListItemsHandler) {

  private lazy val log = Logger(getClass)

  def create(author: Author): Future[AuthorFolder] = {
    log.info(s"$author Creating author folder ...")
    for {
      parentFolderId <- loadTwoLettersAuthorFolder(author.familyName)
      folder <- createAuthorFolder(parentFolderId, author)
      _ <- Future.successful(log.info(s"$author Author folder created $folder"))
    } yield folder
  }

  private def loadTwoLettersAuthorFolder(familyName: String): Future[Int] = {
    val twoLettersFolderName = familyName.substring(0, 2).toLowerCase
    val twoLettersFolderId = authorRootFoldersCache.rootFoldersCache.get(twoLettersFolderName)
    if (twoLettersFolderId.isEmpty) createTwoLettersAuthorFolder(familyName, twoLettersFolderName.capitalize)
    else Future.successful(twoLettersFolderId.get)
  }

  private def createTwoLettersAuthorFolder(familyName: String, twoLettersFolderName: String): Future[Int] = {
    log.info(s"Creating two letters author folder $twoLettersFolderName ...")
    val oneLettersFolderName = familyName.substring(0, 1).toLowerCase
    val oneLettersFolderId = authorRootFoldersCache.rootFoldersCache.get(oneLettersFolderName)
    if (oneLettersFolderId.isEmpty) throw new RuntimeException(s"There is no author folder $oneLettersFolderName")
    folderApi
      .createFolder(oneLettersFolderId.get, twoLettersFolderName, documentTypeAuthor)
      .map(cacheCreatedTwoLettersAuthorFolder(twoLettersFolderName, _))
  }

  private def cacheCreatedTwoLettersAuthorFolder(twoLettersFolderName: String, response: JsValue) = {
    val folderId = (response \ "id").as[Int]
    val cachedFolderId = authorRootFoldersCache.rootFoldersCache.putIfAbsent(twoLettersFolderName.toLowerCase, folderId).getOrElse(folderId)
    if (folderId != cachedFolderId) nodeOperationApi.deleteNode(folderId, includeDescendants = true)
    cachedFolderId
  }

  private def createAuthorFolder(parentFolderId: Int, author: Author): Future[AuthorFolder] =
    for {
      folderName <- buildFolderName(author)
      authorItemId <- authorListItemsHandler.findOrCreate(folderName)
      metadata <- buildFolderMetadata(author, authorItemId)
      resp <- folderApi.createFolder(parentFolderId, folderName, documentTypeAuthor, metadata)
    } yield {
      val id = (resp \ "id").as[Int]
      val name = (resp \ "name").as[String]
      AuthorFolder(CreationStatus.New, id, name, CreationStatus.New, authorItemId)
    }

  private def buildFolderName(author: Author): Future[String] = Future {
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

  private def buildFolderMetadata(author: Author, authorItemId: String): Future[Map[String, String]] = Future {
    Map(
      "givenName" -> author.givenName,
      "familyName" -> author.familyName,
      "initials" -> author.initials,
      listItemsGender -> valueListCache.mapValueListItemId(listItemsGender, author.gender),
      listItemsPrefix -> valueListCache.mapValueListItemId(listItemsPrefix, author.prefix),
      listItemsFamilyNamePrefix -> valueListCache.mapValueListItemId(listItemsFamilyNamePrefix, author.familyNamePrefix),
      listItemsAuthor -> s"[$authorItemId]"
    )
  }

}
