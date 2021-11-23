package service.authormapper.mapper

import components.publishone.DocumentApi
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

  def create(author: Author, folder: AuthorFolder): Future[AuthorDocument] = {
    val documentName = s"${folder.title} (Commentaren)"
    val metadata = buildDocumentMetadata(author)
    documentApi
      .createDocument(folder.id, documentName, documentTypeAuthor, metadata)
      .map { resp =>
        val id = (resp \ "id").as[Int]
        val name = (resp \ "title").as[String]
        AuthorDocument(id, name)
      }
  }

  private def buildDocumentMetadata(author: Author) = {
    Map(
      listItemsRole -> ("[" + publishOneCache.mapListItemValue(listItemsRole, "auteur") + "]"),
      listItemsPublicationName -> ("[" + publishOneCache.mapListItemValue(listItemsPublicationName, author.publicationName) + "]"),
      listItemsPublication -> publishOneCache.mapListItemValue(listItemsPublication, "online")
    )
  }

}
