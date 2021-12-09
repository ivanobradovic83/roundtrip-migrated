package service.authormapper.mapper

import components.publishone.DocumentApi
import play.api.Logger
import play.api.libs.json.JsValue
import service.authormapper.model.{Author, AuthorDocument, AuthorFolder}
import service.common.cache.ValueListCache
import util.CreationStatus
import util.PublishOneConstants.{documentTypeAuthor, _}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Creates document which should contain author details
  *
  * @param documentApi PublishOne Document API
  * @param valueListCache PublishOne cache
  */
class AuthorDocumentCreator @Inject()(documentApi: DocumentApi, valueListCache: ValueListCache) {

  private lazy val log = Logger(getClass)

  def create(author: Author, folder: AuthorFolder): Future[AuthorDocument] = {
    log.info(s"$author $folder Creating author document ...")
    val documentName = s"${folder.title} (Commentaren)"
    val metadata = buildDocumentMetadata(author)
    for {
      document <- documentApi.createDocument(folder.id, documentName, documentTypeAuthor, metadata).map(respToAuthorDocument)
      _ <- documentApi.uploadDocumentContent(document.id, createAuthorDocumentXmlContent(author))
    } yield {
      log.info(s"$author $folder Author document created $document")
      document
    }
  }

  private def createAuthorDocumentXmlContent(author: Author): String =
    s"""<document>
       |  <section orientation="portrait">
       |    <p class="Auteur">
       |      ${author.prefix} ${author.initials} ${author.familyNamePrefix} ${author.familyName}
       |    </p>
       |  </section>
       |</document>""".stripMargin

  private def buildDocumentMetadata(author: Author) =
    Map(
      listItemsRole -> ("[" + valueListCache.mapValueListItemId(listItemsRole, "auteur") + "]"),
      listItemsPublicationName -> ("[" + valueListCache.mapValueListItemId(listItemsPublicationName, author.publicationName) + "]"),
      listItemsPublication -> valueListCache.mapValueListItemId(listItemsPublication, "online")
    )

  private def respToAuthorDocument(resp: JsValue) = {
    val id = (resp \ "id").as[Int]
    val name = (resp \ "title").as[String]
    AuthorDocument(CreationStatus.New, id, name)
  }

}
