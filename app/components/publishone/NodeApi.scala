package components.publishone

import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient
import util.ConfigUtils
import util.StringUtils._
import util.PublishOneConstants.apiNodes

import javax.inject.Inject
import scala.concurrent.Future

class NodeApi @Inject()(configUtils: ConfigUtils, wsClient: WSClient, accessTokenHandler: AccessTokenHandler)
    extends BasicApi(configUtils, wsClient, accessTokenHandler) {

  def getChildNodes(parentNodeId: Int, pageNumber: Int = 1, pageSize: Int = 100): Future[JsValue] = {
    getJson(s"$apiNodes/$parentNodeId/nodes?pageSize=$pageSize&pageNumber=$pageNumber")
  }

  def getNodeMetadata(nodeId: Int): Future[JsValue] = {
    getJson(s"$apiNodes/$nodeId/metadata")
  }

  def getNodesByFamilyGivenNameInitials(familyName: String, givenName: String, initials: String): Future[JsValue] = {
    var url = s"$apiNodes/metadata?familyName=$familyName"
    if (notEmpty(givenName)) url += s"&givenName=$givenName"
    if (notEmpty(initials)) url += s"&initials=$initials"
    getJson(url)
  }

}
