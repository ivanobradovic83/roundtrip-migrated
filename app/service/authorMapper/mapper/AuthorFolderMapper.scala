package service.authorMapper.mapper

import components.publishone.NodeApi
import play.api.Logger
import play.api.libs.json.{JsNull, JsValue, Json, Reads}
import service.authorMapper.model.{Author, AuthorFolder}
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

  type AuthorFolderMeta = (AuthorFolder, (String, String, String))

  type MappingFilter = (Author, AuthorFolderMeta) => Boolean

  def map(author: Author): Future[(Author, Option[AuthorFolder])] = {
    log.info(s"Mapping $author ...")
    mapAuthorToFolder(author, author.familyName, author.givenName).map((author, _))
  }

  private def mapAuthorToFolder(author: Author, familyName: String, givenName: String): Future[Option[AuthorFolder]] = {
    getAuthorFoldersByMetadata(familyName, givenName)
      .flatMap {
        case folders if notEmpty(givenName) && folders.length == 1 => authorFolderFound(author, folders.head)
        case folders if folders.nonEmpty                           => mapAuthorToFolderByFilteringFoldersMetadata(folders, author)
        case _ if notEmpty(givenName)                              => mapAuthorToFolder(author, familyName, null)
        case _                                                     => authorFolderNotFound(author)
      }
  }

  private def authorFolderFound(author: Author, folder: AuthorFolder) = {
    log.info(s"Found mapping folder $folder for $author")
    Future.successful(Option(folder))
  }

  private def authorFolderNotFound(author: Author): Future[Option[AuthorFolder]] = {
    log.info(s"No mapping folder for $author")
    Future.successful(Option.empty)
  }

  private def mappingError(author: Author, filteredAuthorsFolderMetadata: Seq[AuthorFolderMeta]): Future[Option[AuthorFolder]] = {
    log.error(s"Too many folders for $author: $filteredAuthorsFolderMetadata")
    Future.successful(Option.empty)
  }

  private def mapAuthorToFolderByFilteringFoldersMetadata(authorFolders: Seq[AuthorFolder], author: Author): Future[Option[AuthorFolder]] = {
    log.info(s"Trying to map $author by folders metadata $authorFolders ...")
    Future
      .sequence(authorFolders.map(folder => getAuthorFolderMetadata(folder.id)))
      .flatMap(foldersMetadata => {
        val authorsFolderMetadata = authorFolders zip foldersMetadata
        applyFilters(author, authorsFolderMetadata, Seq(filterByFamilyGivenNameInitials, filterByFamilyGivenName, filterByFamilyNameInitials))
      })
  }

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

  @tailrec
  private def applyFilters(author: Author,
                           authorsFolderMetadata: Seq[AuthorFolderMeta],
                           filters: Seq[MappingFilter]): Future[Option[AuthorFolder]] = {
    authorsFolderMetadata.filter(filters.head.apply(author, _)) match {
      case filteredData if filteredData.size > 1  => mappingError(author, filteredData)
      case filteredData if filteredData.size == 1 => authorFolderFound(author, filteredData.head._1)
      case _ if filters.length == 1               => authorFolderNotFound(author)
      case _                                      => applyFilters(author, authorsFolderMetadata, filters.tail)
    }
  }

  private def filterByFamilyGivenNameInitials(author: Author, authorFolderMeta: AuthorFolderMeta) =
    compare(authorFolderMeta._2._1, author.familyName) && compare(authorFolderMeta._2._2, author.givenName) && compare(authorFolderMeta._2._3,
                                                                                                                       author.initials)

  private def filterByFamilyGivenName(author: Author, authorFolderMeta: AuthorFolderMeta) =
    compare(authorFolderMeta._2._1, author.familyName) && compare(authorFolderMeta._2._2, author.givenName)

  private def filterByFamilyNameInitials(author: Author, authorFolderMeta: AuthorFolderMeta) =
    compare(authorFolderMeta._2._1, author.familyName) && compare(authorFolderMeta._2._3, author.initials)

  private def compare(value1: String, value2: String) = {
    if (notEmpty(value1) && notEmpty(value2)) value1.replace("-", " ").equalsIgnoreCase(value2.replace("-", " "))
    else value1 == value2
  }

  private def getAuthorFoldersByMetadata(familyName: String, givenName: String): Future[Seq[AuthorFolder]] =
    nodeApi
      .getNodesByFamilyGivenNameInitials(familyName, givenName, null)
      .map(responseToAuthorFolders)

  private def responseToAuthorFolders(resp: JsValue) =
    resp
      .as[Seq[JsValue]]
      .filter(isAuthorFolder)
      .map(Json.fromJson[AuthorFolder](_).get)

  private def isAuthorFolder(node: JsValue) =
    (node \ "nodeType").as[String] == "folder" &&
      (node \ "documentTypePath").as[String] == "/api/documenttypes/auteursbeschrijvingen"

}
