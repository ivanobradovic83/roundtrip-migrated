package components.publishone

import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient
import util.ConfigUtils
import util.NodeTypes.NodeType

import javax.inject.Inject
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

  def getDocumentMetadataDefinitions: Future[JsValue] = {
    getJson("/api/documenttypes/commentaar/metadata/document")
  }

  def getMetadataDefinitions(documentTypeKey: String, nodeType: NodeType): Future[JsValue] = {
    getJson(s"/api/documenttypes/$documentTypeKey/metadata/$nodeType")
  }

  def getValueListItems(valueListPath: String): Future[JsValue] = {
    getJson(valueListPath)
  }

  def getValueListItems(valueListId: Int): Future[JsValue] = {
    getJson(s"api/valuelists/$valueListId/items")
  }

}
