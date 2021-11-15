package components.publishone

import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient
import util.ConfigUtils

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * PublishOne Metadata API
  *
  * @param configUtils configuration
  * @param wsClient web client
  * @param accessTokenHandler access token handler
  */
class MetadataApi @Inject()(configUtils: ConfigUtils, wsClient: WSClient, accessTokenHandler: AccessTokenHandler)
    extends BasicApi(configUtils, wsClient, accessTokenHandler) {

  def getDocumentMetadataDefinitions(): Future[JsValue] = {
    getJson("/api/documenttypes/commentaar/metadata/document")
  }

  def getValueListItems(valueListPath: String): Future[JsValue] = {
    getJson(valueListPath)
  }

}
