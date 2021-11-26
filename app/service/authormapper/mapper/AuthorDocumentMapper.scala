package service.authormapper.mapper

import components.publishone.{MetadataApi, NodeApi}
import play.api.Logger
import play.api.libs.json.JsValue
import service.authormapper.cache.PublishOneCache
import service.authormapper.model.{Author, AuthorDocument, AuthorFolder}
import util.PublishOneConstants.listItemsPublicationName
import util.PublishOneUtils.responseToAuthorDocuments
import util.PublishOneUtils._

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Tries to map given author to it's corresponding document in PublishOne author folder
  *
  * @param nodeApi PublishOne Node API
  * @param metadataApi PublishOne Metadata API
  * @param publishOneCache PublishOne cache
  */
class AuthorDocumentMapper @Inject()(nodeApi: NodeApi, metadataApi: MetadataApi, publishOneCache: PublishOneCache) {

  private lazy val log = Logger(getClass)

  def map(author: Author, folder: AuthorFolder): Future[Option[AuthorDocument]] = {
    log.info(s"$author $folder Mapping author document started")
    val publicationId = publishOneCache.mapListItemValue(listItemsPublicationName, author.publicationName)
    for {
      authorDocuments <- getAuthorDocuments(folder.id)
      authorDocument <- mapAuthorsToDocuments(publicationId, authorDocuments)
      _ <- Future.successful(log.info(s"author $folder Author mapped to $authorDocument"))
    } yield authorDocument
  }

  private def mapAuthorsToDocuments(publicationId: String, authorDocuments: Seq[AuthorDocument]): Future[Option[AuthorDocument]] =
    metadataApi.getDocumentMetadata(authorDocuments.head.id).flatMap { metadata =>
      val isMatched = matchPublicationName(publicationId, metadata)
      if (isMatched) Future.successful(Some(authorDocuments.head))
      else if (authorDocuments.size == 1) Future.successful(None)
      else mapAuthorsToDocuments(publicationId, authorDocuments.tail)
    }

  private def matchPublicationName(publicationId: String, metadata: JsValue): Boolean = {
    getMetadataFields(metadata)
      .filter(meta => listItemsPublicationName == (meta \ "name").as[String])
      .map { field =>
        val fieldValue = (field \ "value").get.toString()
        if (fieldValue == "\"[" + publicationId + "]\"") true
        else false
      }
      .headOption
      .getOrElse(false)
  }

  private def getAuthorDocuments(folderId: Int): Future[Seq[AuthorDocument]] =
    nodeApi.getChildNodes(folderId).map(nodesResponse => responseToAuthorDocuments((nodesResponse \ "items").get))

}
