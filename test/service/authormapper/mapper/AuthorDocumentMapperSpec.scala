package service.authormapper.mapper

import components.publishone.{MetadataApi, NodeApi}
import helpers.ScalaSpec
import helpers.TestUtils.mockedJsonResp
import org.mockito.Mockito.{reset, when}
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import service.authormapper.model.{Author, AuthorDocument, AuthorFolder}
import service.common.cache.ValueListCache
import util.CreationStatus
import util.PublishOneConstants.listItemsPublicationName

import scala.concurrent.Await

class AuthorDocumentMapperSpec extends ScalaSpec {

  lazy val author: Author = Author("J133", "E.S. de Jong", "Jong", "Eva", "E.S.", "de", "mr.", "f", "c-ar", "c-AR-W3936-610")
  lazy val folder: AuthorFolder = AuthorFolder(CreationStatus.New, 44462, "Jong E.S. de", CreationStatus.New, "8857")
  lazy val document: AuthorDocument = AuthorDocument(CreationStatus.Existing, 45105, "Zondag, W.A. (Commentaren)")
  lazy val documentId: Int = document.id

  val nodeApi: NodeApi = mock[NodeApi]
  val metadataApi: MetadataApi = mock[MetadataApi]
  val valueListCache: ValueListCache = mock[ValueListCache]
  val cut = new AuthorDocumentMapper(nodeApi, metadataApi, valueListCache)

  override def beforeEach(): Unit = {
    reset(nodeApi)
    reset(metadataApi)
    reset(valueListCache)
  }

  "when publicationName is correct then map author and document" in {
    when(valueListCache.mapValueListItemId(listItemsPublicationName, author.publicationName)).thenReturn("4456")
    when(nodeApi.getChildNodes(folder.id)).thenReturn(mockedJsonResp("child-nodes"))
    when(metadataApi.getDocumentMetadata(documentId)).thenReturn(mockedJsonResp("metadata-comment-document"))
    val result = Await.result(cut.map(author, folder), 10.seconds)
    result should be(Some(document))
  }

  "when publicationName is not correct then return none" in {
    when(valueListCache.mapValueListItemId(listItemsPublicationName, author.publicationName)).thenReturn("1234")
    when(nodeApi.getChildNodes(folder.id)).thenReturn(mockedJsonResp("child-nodes"))
    when(metadataApi.getDocumentMetadata(documentId)).thenReturn(mockedJsonResp("metadata-comment-document"))
    val result = Await.result(cut.map(author, folder), 10.seconds)
    result should be(None)
  }

}
