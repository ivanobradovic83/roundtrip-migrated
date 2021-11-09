package components.publishone

import common.PublishOneConstants._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

/**
  * PublishOne Folder API
  *
  * @param config configuration
  * @param wsClient web client
  * @param accessTokenHandler access token handler
  */
class FolderApi @Inject()(config: Configuration, wsClient: WSClient, accessTokenHandler: AccessTokenHandler)
    extends BasicApi(config, wsClient, accessTokenHandler) {

  def getFolderById(id: Int): Future[JsValue] = {
    getJson(s"$apiFolders/$id")
  }

}
