package components.publishone

import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import util.ConfigUtils
import util.PublishOneConstants._

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

  def createDocument(parentId: Int, name: String, docType: String, metadata: Map[String, String]): Future[JsValue] = {
    val requestBody = createDocumentRequestBody(parentId, name, docType, metadata)
    postJson(apiDocuments, requestBody)
  }

  def uploadDocumentContent(docId: Int, content: String): Future[Unit] = {
    putXml(s"$apiDocuments/$docId/xml", content)
  }

  private def createDocumentRequestBody(parentId: Int, name: String, docType: String, metadata: Map[String, String]) = {
    val jsonMetadata = metadata.map {
      case (key, value) =>
        Json.obj(
          "name" -> key,
          "value" -> value,
          "updateOperation" -> "replace"
        )
    }
    Json.obj(
      "parentId" -> parentId,
      "name" -> name,
      "documentTypePath" -> docType,
      "metadataFields" -> jsonMetadata
    )
  }

}
