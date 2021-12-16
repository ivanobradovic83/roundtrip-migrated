package components.publishone

import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import util.ConfigUtils
import util.PublishOneConstants._
import util.PublishOneUtils._

import javax.inject.Inject
import scala.concurrent.Future

/** PublishOne Links API
  *
  * @param configUtils
  *   configuration
  * @param wsClient
  *   web client
  * @param accessTokenHandler
  *   access token handler
  */
class LinkApi @Inject()(configUtils: ConfigUtils, wsClient: WSClient, accessTokenHandler: AccessTokenHandler)
    extends BasicApi(configUtils, wsClient, accessTokenHandler) {

  def createInternalLink(folderId: Int, name: String, docType: String, docId: Int): Future[JsValue] = {
    val requestBody = createInternalLinkRequestBody(folderId, name, docType, docId)
    postJson(apiLinks, requestBody)
  }

  private def createInternalLinkRequestBody(folderId: Int, name: String, docType: String, docId: Int) = {
    Json.obj(
      "parentId" -> folderId,
      "name" -> name,
      "documentTypePath" -> docTypePath(docType),
      "url" -> docId.toString,
      "type" -> "internal"
    )
  }

}
