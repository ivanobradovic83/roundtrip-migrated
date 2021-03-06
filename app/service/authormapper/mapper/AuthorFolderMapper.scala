package service.authormapper.mapper

import components.publishone.NodeApi
import play.api.Logger
import play.api.libs.json.{JsNull, JsValue}
import service.authormapper.model.{Author, AuthorFolder}
import util.PublishOneConstants.listItemsAuthor
import util.PublishOneUtils._
import util.StringUtils.notEmpty

import javax.inject.Inject
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/** Tries to map given author (collected from SWS document metadata) to PublishOne author folder
  *
  * @param nodeApi
  *   PublishOne Node API
  */
class AuthorFolderMapper @Inject()(nodeApi: NodeApi) {

  private lazy val log = Logger(getClass)
  case class FolderMetadata(familyName: String, givenName: String, initials: String, authorItemId: String)
  type MappingFilter = (Author, FolderMetadata) => Boolean
  lazy val filters: Seq[MappingFilter] = Seq[MappingFilter](filterByFamilyGivenNameInitials, filterByFamilyGivenName, filterByFamilyNameInitials)

  def map(author: Author): Future[Option[AuthorFolder]] = {
    log.info(s"$author Mapping author folder ...")
    mapAuthorToFolder(author, author.familyName, author.givenName)
  }

  private def mapAuthorToFolder(author: Author, familyName: String, givenName: String): Future[Option[AuthorFolder]] =
    getAuthorFoldersByMetadata(familyName, givenName)
      .flatMap {
        case folders if notEmpty(givenName) && folders.length == 1 => setAuthorItemIdOnFoundFolder(author, folders.head)
        case folders if folders.nonEmpty                           => mapAuthorToFolderByFilteringFoldersMetadata(folders, author)
        case _ if notEmpty(givenName)                              => mapAuthorToFolder(author, familyName, null)
        case _                                                     => folderNotFound(author)
      }

  private def setAuthorItemIdOnFoundFolder(author: Author, folder: AuthorFolder): Future[Option[AuthorFolder]] =
    getAuthorFolderMetadata(folder.id)
      .flatMap { folderMetadata =>
        folder.authorItemId = folderMetadata.authorItemId
        folderFound(author, folder)
      }

  private def mapAuthorToFolderByFilteringFoldersMetadata(folders: Seq[AuthorFolder], author: Author): Future[Option[AuthorFolder]] = {
    log.info(s"Trying to map $author by folders metadata $folders ...")
    getAllFoldersMetadata(folders).map(applyFilters(author, _, filters))
  }

  private def getAllFoldersMetadata(folders: Seq[AuthorFolder]) =
    Future.sequence(folders.map(folder => getAuthorFolderMetadata(folder.id))).map(folders zip _)

  @tailrec
  private def applyFilters(
      author: Author,
      foldersMetadata: Seq[(AuthorFolder, FolderMetadata)],
      filters: Seq[MappingFilter]
  ): Option[AuthorFolder] =
    applyFilter(author, foldersMetadata, filters.head) match {
      case Some((folder, metadata)) =>
        folder.authorItemId = metadata.authorItemId
        Some(folder)
      case None if filters.size == 1 => None
      case _                         => applyFilters(author, foldersMetadata, filters.tail)
    }

  private def applyFilter(author: Author, foldersMetadata: Seq[(AuthorFolder, FolderMetadata)], filter: MappingFilter) =
    foldersMetadata.find { case (_, metadata) => filter.apply(author, metadata) }

  private def filterByFamilyGivenNameInitials(author: Author, folderMetadata: FolderMetadata) =
    compare(folderMetadata.familyName, author.familyName) && compare(folderMetadata.givenName, author.givenName) && compare(
      folderMetadata.initials,
      author.initials
    )

  private def filterByFamilyGivenName(author: Author, folderMetadata: FolderMetadata) =
    compare(folderMetadata.familyName, author.familyName) && compare(folderMetadata.givenName, author.givenName)

  private def filterByFamilyNameInitials(author: Author, folderMetadata: FolderMetadata) =
    compare(folderMetadata.familyName, author.familyName) && compare(folderMetadata.initials, author.initials)

  private def compare(value1: String, value2: String) = {
    if (notEmpty(value1) && notEmpty(value2)) value1.replace("-", " ").equalsIgnoreCase(value2.replace("-", " "))
    else value1 == value2
  }

  private def getAuthorFoldersByMetadata(familyName: String, givenName: String): Future[Seq[AuthorFolder]] =
    nodeApi.getNodesByFamilyGivenNameInitials(familyName, givenName, null).map(responseToAuthorFolders)

  private def getAuthorFolderMetadata(folderId: Int): Future[FolderMetadata] =
    nodeApi.getNodeMetadata(folderId).map(transformToFamilyGivenNameAndInitials)

  private def transformToFamilyGivenNameAndInitials(response: JsValue): FolderMetadata = {
    val metadataFields = (response \\ "metadataFields").toSeq.flatMap(_.as[Seq[JsValue]])
    val familyName = getMetadataValue(metadataFields, "familyName")
    val givenName = getMetadataValue(metadataFields, "givenName")
    val initials = getMetadataValue(metadataFields, "initials")
    val authorItemId = getMetadataValue(metadataFields, listItemsAuthor)

    FolderMetadata(familyName, givenName, initials, authorItemId.replaceAll("[\\[\\]]", ""))
  }

  private def getMetadataValue(metadata: Seq[JsValue], metadataName: String): String = {
    val givenNameMetadata = metadata.filter(metadata => (metadata \ "name").as[String] == metadataName).head
    val givenNameMetadataValue = (givenNameMetadata \ "value").getOrElse(JsNull)
    if (givenNameMetadataValue != JsNull) givenNameMetadataValue.as[String]
    else ""
  }

  private def folderFound(author: Author, folder: AuthorFolder): Future[Option[AuthorFolder]] = {
    log.info(s"Found mapping folder $folder for $author")
    Future.successful(Some(folder))
  }

  private def folderNotFound(author: Author): Future[Option[AuthorFolder]] = {
    log.info(s"No mapping folder for $author")
    Future.successful(Option.empty[AuthorFolder])
  }

}
