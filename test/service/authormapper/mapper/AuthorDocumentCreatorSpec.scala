package service.authormapper.mapper

import components.publishone.DocumentApi
import helpers.ScalaSpec
import helpers.TestUtils.mockedJsonResp
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import service.authormapper.model.{Author, AuthorDocument, AuthorFolder}
import service.common.cache.ValueListCache
import util.PublishOneConstants._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class AuthorDocumentCreatorSpec extends ScalaSpec {

  lazy val author: Author = Author("J133", "E.S. de Jong", "Jong", "Eva", "E.S.", "de", "mr.", "f", "c-ar", "c-AR-W3936-610")
  lazy val folder: AuthorFolder = AuthorFolder(44462, "Jong E.S. de", "8857")
  lazy val document: AuthorDocument = AuthorDocument(1234, "Document title")
  lazy val documentId = 1234
  lazy val documentTitle = "Document title"
  lazy val documentContent: String =
    """<document>
      |  <section orientation="portrait">
      |    <p class="Auteur">
      |      mr. E.S. de Jong
      |    </p>
      |  </section>
      |</document>""".stripMargin

  lazy val documentMetadata = Map(
    listItemsRole -> "[123]",
    listItemsPublicationName -> "[456]",
    listItemsPublication -> "789"
  )

  val documentApi: DocumentApi = mock[DocumentApi]
  val valueListCache: ValueListCache = mock[ValueListCache]
  val cut = new AuthorDocumentCreator(documentApi, valueListCache)

  override def beforeEach(): Unit = {
    reset(documentApi)
    reset(valueListCache)
  }

  "when author and folder are valid then create document and set content" in {
    when(valueListCache.mapValueListItemId(listItemsRole, "auteur")).thenReturn("123")
    when(valueListCache.mapValueListItemId(listItemsPublicationName, author.publicationName)).thenReturn("456")
    when(valueListCache.mapValueListItemId(listItemsPublication, "online")).thenReturn("789")
    when(documentApi.createDocument(44462, "Jong E.S. de (Commentaren)", documentTypeAuthor, documentMetadata))
      .thenReturn(mockedJsonResp("create-document"))
    when(documentApi.uploadDocumentContent(any(), any())).thenReturn(Future.unit)

    val result = Await.result(cut.create(author, folder), 10.seconds)
    result should be(document)

    verify(documentApi).uploadDocumentContent(documentId, documentContent)
  }
}
