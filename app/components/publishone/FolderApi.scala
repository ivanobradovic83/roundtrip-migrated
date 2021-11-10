package components.publishone

import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient
import util.ConfigUtils
import util.PublishOneConstants._

import javax.inject.Inject
import scala.concurrent.Future

/**
  * PublishOne Folder API
  *
  * @param configUtils configuration
  * @param wsClient web client
  * @param accessTokenHandler access token handler
  */
class FolderApi @Inject()(configUtils: ConfigUtils, wsClient: WSClient, accessTokenHandler: AccessTokenHandler)
    extends BasicApi(configUtils, wsClient, accessTokenHandler) {

  def getFolderById(id: Int): Future[JsValue] = {
    getJson(s"$apiFolders/$id")
  }

}
