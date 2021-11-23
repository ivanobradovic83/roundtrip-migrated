package service.authormapper.mapper

import components.publishone.NodeApi
import play.api.Logger
import play.api.libs.json.{JsNull, JsValue, Json, Reads}
import service.authormapper.model.{Author, AuthorFolder}
import util.PublishOneUtils._
import util.StringUtils.notEmpty

import javax.inject.Inject
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Tries to map given author (collected from SWS document metadata) to PublishOne author folder
  *
  * @param nodeApi PublishOne Node API
  */
class AuthorFolderMapper @Inject()(nodeApi: NodeApi) {

  private lazy val log = Logger(getClass)
  private implicit val authorFolderReads: Reads[AuthorFolder] = Json.reads[AuthorFolder]

  type FolderMetadata = (String, String, String)

  type MappingFilter = (Author, FolderMetadata) => Boolean

  def map(author: Author): Future[(Author, Option[AuthorFolder])] = {
    log.info(s"Mapping $author ...")
    mapAuthorToFolder(author, author.familyName, author.givenName).map((author, _))
  }

  private def mapAuthorToFolder(author: Author, familyName: String, givenName: String): Future[Option[AuthorFolder]] = {
    getAuthorFoldersByMetadata(familyName, givenName)
      .flatMap {
        case folders if notEmpty(givenName) && folders.length == 1 => folderFound(author, folders.head)
        case folders if folders.nonEmpty                           => mapAuthorToFolderByFilteringFoldersMetadata(folders, author)
        case _ if notEmpty(givenName)                              => mapAuthorToFolder(author, familyName, null)
        case _                                                     => folderNotFound(author)
      }
  }

  private def mapAuthorToFolderByFilteringFoldersMetadata(folders: Seq[AuthorFolder], author: Author): Future[Option[AuthorFolder]] = {
    log.info(s"Trying to map $author by folders metadata $folders ...")
    getAuthorFolderMetadata(folders.head.id)
      .flatMap { folderMetadata =>
        val filters = Seq[MappingFilter](filterByFamilyGivenNameInitials, filterByFamilyGivenName, filterByFamilyNameInitials)
        applyFilters(author, folders.head, folderMetadata, filters) match {
          case Some(mappedFolder)        => Future.successful(Option(mappedFolder))
          case None if folders.size == 1 => Future.successful(Option.empty)
          case None                      => mapAuthorToFolderByFilteringFoldersMetadata(folders.tail, author)
        }
      }
  }

  @tailrec
  private def applyFilters(author: Author,
                           folder: AuthorFolder,
                           folderMetadata: FolderMetadata,
                           filters: Seq[MappingFilter]): Option[AuthorFolder] = {
    filters.head.apply(author, folderMetadata) match {
      case true                       => Option(folder)
      case false if filters.size == 1 => Option.empty
      case _                          => applyFilters(author, folder, folderMetadata, filters.tail)
    }
  }

  private def filterByFamilyGivenNameInitials(author: Author, folderMetadata: FolderMetadata) =
    compare(folderMetadata._1, author.familyName) && compare(folderMetadata._2, author.givenName) && compare(folderMetadata._3, author.initials)

  private def filterByFamilyGivenName(author: Author, folderMetadata: FolderMetadata) =
    compare(folderMetadata._1, author.familyName) && compare(folderMetadata._2, author.givenName)

  private def filterByFamilyNameInitials(author: Author, folderMetadata: FolderMetadata) =
    compare(folderMetadata._1, author.familyName) && compare(folderMetadata._3, author.initials)

  private def compare(value1: String, value2: String) = {
    if (notEmpty(value1) && notEmpty(value2)) value1.replace("-", " ").equalsIgnoreCase(value2.replace("-", " "))
    else value1 == value2
  }

  private def getAuthorFoldersByMetadata(familyName: String, givenName: String): Future[Seq[AuthorFolder]] =
    nodeApi.getNodesByFamilyGivenNameInitials(familyName, givenName, null).map(responseToAuthorFolders)

  private def getAuthorFolderMetadata(folderId: Int): Future[(String, String, String)] =
    nodeApi.getNodeMetadata(folderId).map(transformToFamilyGivenNameAndInitials)

  private def transformToFamilyGivenNameAndInitials(response: JsValue): (String, String, String) = {
    val metadataFields = (response \\ "metadataFields").toSeq.flatMap(_.as[Seq[JsValue]])
    val familyName = getMetadataValue(metadataFields, "familyName")
    val givenName = getMetadataValue(metadataFields, "givenName")
    val initials = getMetadataValue(metadataFields, "initials")
    (familyName, givenName, initials)
  }

  private def getMetadataValue(metadata: Seq[JsValue], metadataName: String): String = {
    val givenNameMetadata = metadata.filter(metadata => (metadata \ "name").as[String] == metadataName).head
    val givenNameMetadataValue = (givenNameMetadata \ "value").getOrElse(JsNull)
    if (givenNameMetadataValue != JsNull) givenNameMetadataValue.as[String]
    else ""
  }

  private def folderFound(author: Author, folder: AuthorFolder) = {
    log.info(s"Found mapping folder $folder for $author")
    Future.successful(Option(folder))
  }

  private def folderNotFound(author: Author) = {
    log.info(s"No mapping folder for $author")
    Future.successful(Option.empty)
  }

}
