package components.publishone

import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import util.ConfigUtils
import util.PublishOneConstants._

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.Future

/**
  * PublishOne Document API
  *
  * @param configUtils configuration
  * @param wsClient web client
  * @param accessTokenHandler access token handler
  */
class DocumentApi @Inject()(configUtils: ConfigUtils, wsClient: WSClient, accessTokenHandler: AccessTokenHandler)
    extends BasicApi(configUtils, wsClient, accessTokenHandler) {

  def createDocument(parentId: Int, name: String, docType: String): Future[JsValue] = {
    val requestBody = createDocumentRequestBody(parentId, name, docType)
    postJson(apiDocuments, requestBody)
  }

  def uploadDocumentContent(docId: Int, content: String): Future[WSResponse] = {
    putXml(s"$apiDocuments/$docId/xml", content)
  }

  private def createDocumentRequestBody(parentId: Int, name: String, docType: String) = {
    val editorialDate = Json.obj(
      "name" -> "editorialDate",
      "value" -> LocalDate.now(),
      "updateOperation" -> "replace"
    )
    Json.obj(
      "parentId" -> parentId,
      "name" -> name,
      "documentTypePath" -> docType,
      "metadataFields" -> Seq(editorialDate)
    )
  }

}
