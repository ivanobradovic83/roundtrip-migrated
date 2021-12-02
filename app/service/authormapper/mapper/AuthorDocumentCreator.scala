package service.authormapper.mapper

import components.publishone.DocumentApi
import play.api.Logger
import play.api.libs.json.JsValue
import service.authormapper.cache.ValueListCache
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
class AuthorDocumentCreator @Inject()(documentApi: DocumentApi, publishOneCache: ValueListCache) {

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
      listItemsRole -> ("[" + publishOneCache.mapValueListItemId(listItemsRole, "auteur") + "]"),
      listItemsPublicationName -> ("[" + publishOneCache.mapValueListItemId(listItemsPublicationName, author.publicationName) + "]"),
      listItemsPublication -> publishOneCache.mapValueListItemId(listItemsPublication, "online")
    )
  }

  private def createDocumentRespToAuthorDocument(resp: JsValue) = {
    val id = (resp \ "id").as[Int]
    val name = (resp \ "title").as[String]
    val document = AuthorDocument(id, name)
    document
  }

}
