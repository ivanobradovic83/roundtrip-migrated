package service.authormapper.mapper

import components.publishone.DocumentApi
import play.api.Logger
import play.api.libs.json.JsValue
import service.authormapper.cache.PublishOneCache
import service.authormapper.model.{Author, AuthorDocument, AuthorFolder}
import util.PublishOneConstants._

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Creates document which should contain author details
  *
  * @param documentApi PublishOne Document API
  * @param publishOneCache PublishOne cache
  */
class AuthorDocumentCreator @Inject()(documentApi: DocumentApi, publishOneCache: PublishOneCache) {

  private lazy val log = Logger(getClass)

  def create(author: Author, folder: AuthorFolder): Future[AuthorDocument] = {
    log.info(s"$author $folder Creating author document ...")
    val documentName = s"${folder.title} (Commentaren)"
    val metadata = buildDocumentMetadata(author)
    documentApi
      .createDocument(folder.id, documentName, documentTypeAuthor, metadata)
      .map { resp =>
        val document = createDocumentRespToAuthorDocument(resp)
        log.info(s"$author $folder Author document created $document")
        document
      }
  }

  private def buildDocumentMetadata(author: Author) = {
    Map(
      listItemsRole -> ("[" + publishOneCache.mapListItemValue(listItemsRole, "auteur") + "]"),
      listItemsPublicationName -> ("[" + publishOneCache.mapListItemValue(listItemsPublicationName, author.publicationName) + "]"),
      listItemsPublication -> publishOneCache.mapListItemValue(listItemsPublication, "online")
    )
  }

  private def createDocumentRespToAuthorDocument(resp: JsValue) = {
    val id = (resp \ "id").as[Int]
    val name = (resp \ "title").as[String]
    val document = AuthorDocument(id, name)
    document
  }

}
