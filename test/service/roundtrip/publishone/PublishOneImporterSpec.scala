package service.roundtrip.publishone

import components.publishone.{DocumentApi, FolderApi, LinkApi, NodeOperationApi}
import helpers.ScalaSpec
import helpers.TestUtils.{emptyJson, mockedJsonResp}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import service.roundtrip.model.{AuthorDocumentMapping, RoundTripDocument}
import util.PublishOneConstants.listItemsAuthor

import java.util.Date
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class PublishOneImporterSpec extends ScalaSpec {

  lazy val createdFolderId = 4523
  lazy val createdDocumentId = 1234
  lazy val docKey: String = "doc_key"
  lazy val docType: String = "doc_type"
  lazy val destination: String = "123"
  lazy val authorDoc1Id = 7001
  lazy val authorDoc1Title = "doc_title_1"
  lazy val authorDoc1ItemId = "71001"
  lazy val authorDoc2Id = 7002
  lazy val authorDoc2Title = "doc_title_2"
  lazy val authorDoc2ItemId = "71002"
  lazy val roundTripDoc: RoundTripDocument = RoundTripDocument("rt_id", docKey, docType, destination)
  lazy val docContent: String = "document content"
  lazy val docContentByte: Array[Byte] = docContent.getBytes
  lazy val folderMetadata: Map[String, AnyRef] = Map("key11" -> "val11", "key12" -> new Date())
  lazy val docMetadata: Map[String, AnyRef] = Map(
    "key21" -> "val21",
    listItemsAuthor -> Seq(AuthorDocumentMapping(authorDoc1Id, authorDoc1Title, authorDoc1ItemId),
                           AuthorDocumentMapping(authorDoc2Id, authorDoc2Title, authorDoc2ItemId))
  )
  lazy val folderMetadataString: Map[String, String] = Map("key11" -> "val11")
  lazy val docMetadataString: Map[String, String] = Map("key21" -> "val21", listItemsAuthor -> s"[$authorDoc1ItemId,$authorDoc2ItemId]")

  val folderApi: FolderApi = mock[FolderApi]
  val documentApi: DocumentApi = mock[DocumentApi]
  val nodeOpsApi: NodeOperationApi = mock[NodeOperationApi]
  val linkApi: LinkApi = mock[LinkApi]
  val cut: PublishOneImporter = new PublishOneImporter(folderApi, documentApi, nodeOpsApi, linkApi)

  override def beforeEach(): Unit = {
    reset(folderApi)
    reset(documentApi)
    reset(nodeOpsApi)
    reset(linkApi)
  }

  "when CWC document prepared then import it to PublishOne" in {
    when(folderApi.createFolder(destination.toInt, docKey, docType, folderMetadataString)).thenReturn(mockedJsonResp("create-folder"))
    when(documentApi.createDocument(createdFolderId, docKey, docType, docMetadataString)).thenReturn(mockedJsonResp("create-document"))
    when(documentApi.uploadDocumentContent(any(), any())).thenReturn(Future.unit)
    when(linkApi.createInternalLink(any(), any(), any(), any())).thenReturn(emptyJson)

    val result = Await.result(cut.importDocument(roundTripDoc, docContentByte, folderMetadata, docMetadata), 10.seconds)
    result.folderId should be(createdFolderId)
    result.folderName should be(docKey)
    result.documentIds should contain only createdDocumentId
    verify(documentApi).uploadDocumentContent(createdDocumentId, docContent)
    verify(linkApi).createInternalLink(createdFolderId, authorDoc1Title, docType, authorDoc1Id)
    verify(linkApi).createInternalLink(createdFolderId, authorDoc2Title, docType, authorDoc2Id)
  }

}
