package service.roundtrip.publishone

import akka.util.ByteString
import components.publishone.{NodeOperationApi, PublicationApi}
import helpers.ScalaSpec
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import play.api.libs.ws.WSResponse
import service.roundtrip.model.{ImportedDocument, RoundTripDocument}
import util.ConfigUtils

import java.io.File
import java.nio.file.Files
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class PublishOnePublisherSpec extends ScalaSpec {

  lazy val folderId = 4523
  lazy val docId = 1234
  lazy val docKey: String = "doc_key"
  lazy val roundTripDoc: RoundTripDocument = RoundTripDocument("rt_id", docKey, "doc_type", "destination")
  lazy val importedDoc: ImportedDocument = ImportedDocument(folderId, docKey, Seq(docId))
  lazy val createPublicationTicket: String = "create_publication_ticket"
  lazy val resultId: String = "success"
  lazy val publicationProfile = "publication_profile_id"
  lazy val docStateCreated = 956
  lazy val docStatePublish = 957

  val wsResponse: WSResponse = mock[WSResponse]
  val configUtils: ConfigUtils = mock[ConfigUtils]
  val publicationApi: PublicationApi = mock[PublicationApi]
  val nodeOpsApi: NodeOperationApi = mock[NodeOperationApi]
  val cut: PublishOnePublisher = new PublishOnePublisher(configUtils, publicationApi, nodeOpsApi)

  override def beforeEach(): Unit = {
    reset(wsResponse)
    reset(configUtils)
    reset(publicationApi)
    reset(nodeOpsApi)
  }

  override def afterEach(): Unit = {
    val testFile = new File("filename.zip")
    if (testFile.exists()) Files.delete(new File("filename.zip").toPath)
  }

  "when CWC document imported to PublishOne then republish it" in {
    when(configUtils.publicationProfileOnlineZip).thenReturn(publicationProfile)
    when(configUtils.documentStateCreated).thenReturn(docStateCreated)
    when(configUtils.documentStatePublish).thenReturn(docStatePublish)
    when(nodeOpsApi.changeState(any(), any(), any())).thenReturn(Future.successful(true))
    when(publicationApi.createPublication(publicationProfile, folderId, docKey))
      .thenReturn(Future.successful((createPublicationTicket, resultId)))
    when(publicationApi.getFinishedPublicationFile(publicationProfile, createPublicationTicket, resultId)).thenReturn(Future.successful(wsResponse))
    when(publicationApi.deletePublication(any(), any())).thenReturn(Future.unit)
    when(wsResponse.header("Content-disposition")).thenReturn(Some("attachment; filename=filename.zip;"))
    when(wsResponse.bodyAsBytes).thenReturn(ByteString("publication_content".getBytes))

    Await.result(cut.publish(roundTripDoc, importedDoc), 10.seconds)
    verify(nodeOpsApi).changeState(docId, docStateCreated, docStatePublish)
    verify(publicationApi).deletePublication(publicationProfile, createPublicationTicket)
  }

}
