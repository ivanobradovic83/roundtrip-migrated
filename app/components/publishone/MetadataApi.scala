package components.publishone

import akka.stream.scaladsl.{FileIO, Source}
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient
import play.api.mvc.MultipartFormData.FilePart
import util.ConfigUtils
import util.NodeTypes.NodeType
import util.PublishOneConstants._

import java.io.{ByteArrayInputStream, File}
import java.nio.file.{Files, StandardCopyOption}
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/** PublishOne Metadata API
  *
  * @param configUtils
  *   configuration
  * @param wsClient
  *   web client
  * @param accessTokenHandler
  *   access token handler
  */
class MetadataApi @Inject()(configUtils: ConfigUtils, wsClient: WSClient, accessTokenHandler: AccessTokenHandler)
    extends BasicApi(configUtils, wsClient, accessTokenHandler) {

  def getMetadataDefinitions(documentTypeKey: String, nodeType: NodeType): Future[JsValue] =
    getJson(s"$apiDocumentTypes/$documentTypeKey/metadata/$nodeType")

  def getValueListItems(valueListPath: String): Future[JsValue] = getJson(valueListPath)

  def getValueListItems(valueListId: Int): Future[JsValue] = getJson(s"$apiValueLists/$valueListId/items")

  def getDocumentMetadata(docId: Int): Future[JsValue] = getJson(s"$apiNodes/$docId/metadata")

  def createValueListItem(valueListId: Int, xmlContent: String): Future[Unit] = {
    val tempFile = File.createTempFile("author-add-list-item-", ".xml")
    Files.copy(new ByteArrayInputStream(xmlContent.getBytes), tempFile.toPath, StandardCopyOption.REPLACE_EXISTING)
    val filePart = FilePart("file", tempFile.getName, Some("text/xml"), FileIO.fromPath(tempFile.toPath))

    post(s"$apiValueLists/$valueListId/items", requestBody = Source(filePart :: List()))
      .map(resp => { Files.delete(tempFile.toPath); resp })
      .map(handleNoContentResponse)
  }

}
