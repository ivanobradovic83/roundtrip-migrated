package components.publishone

import play.api.libs.json.{JsValue, Json}
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

  def createFolder(parentId: Int, name: String, docType: String): Future[JsValue] =
    createFolder(parentId, name, docType, Map.empty)

  def createFolder(parentId: Int, name: String, docType: String, metadata: Map[String, String]): Future[JsValue] = {
    val requestBody = createFolderRequestBody(parentId, name, docType, metadata)
    postJson(apiFolders, requestBody)
  }

  private def createFolderRequestBody(parentId: Int, name: String, docType: String, metadata: Map[String, String]) = {
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
      "projectShortTitle" -> name,
      "metadataFields" -> jsonMetadata
    )
  }

}
