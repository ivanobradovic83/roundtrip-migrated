package components.publishone

import common.PublishOneConstants._
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.Future

/**
  * PublishOne Document API
  *
  * @param config configuration
  * @param wsClient web client
  * @param accessTokenHandler access token handler
  */
class DocumentApi @Inject()(config: Configuration, wsClient: WSClient, accessTokenHandler: AccessTokenHandler)
    extends BasicApi(config, wsClient, accessTokenHandler) {

  def createDocument(folderId: Int, docName: String, docType: String): Future[JsValue] = {
    val requestBody = createDocumentRequestBody(folderId, docName, docType)
    postJson(apiDocuments, requestBody)
  }

  def uploadDocumentContent(docId: Int, content: String): Future[JsValue] = {
    putXml(s"$apiDocuments/$docId/xml", content)
  }

  private def createDocumentRequestBody(folderId: Int, docName: String, docType: String) = {
    val editorialDate = Json.obj(
      "name" -> "editorialDate",
      "value" -> LocalDate.now(),
      "updateOperation" -> "replace"
    )
    Json.obj(
      "parentId" -> folderId,
      "name" -> docName,
      "documentTypePath" -> docType,
      "metadataFields" -> Seq(editorialDate)
    )
  }

}
