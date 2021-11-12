package components.publishone

import components.common.ErrorResponseHandler
import play.api.Logger
import play.api.http.Status.{CREATED, NO_CONTENT, OK}
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import util.ConfigUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * This class contains basic methods for Rest communication with PublishOne
  *
  * @param configUtils configuration
  * @param wsClient web client
  * @param accessTokenHandler access token handler
  */
abstract class BasicApi(configUtils: ConfigUtils, wsClient: WSClient, accessTokenHandler: AccessTokenHandler) {

  private lazy val log = Logger(getClass)

  protected def postJson(relativeUrl: String, requestBody: JsObject): Future[JsValue] = {
    val putApiCall = (at: String) =>
      createWsRequest(relativeUrl, at)
        .addHttpHeaders("Content-Type" -> "application/json")
        .post(requestBody)
    executeRequest(putApiCall).map(handleJsonResponse)
  }

  protected def putXml(relativeUrl: String, requestBody: String): Future[WSResponse] = {
    val putApiCall = (at: String) =>
      createWsRequest(relativeUrl, at)
        .addHttpHeaders("Content-Type" -> "application/xml")
        .put(requestBody)
    executeRequest(putApiCall)
  }

  protected def getJson(relativeUrl: String): Future[JsValue] = {
    val getApiCall = (at: String) =>
      createWsRequest(relativeUrl, at)
        .addHttpHeaders("Accept" -> "application/json")
        .get()
    executeRequest(getApiCall).map(handleJsonResponse)
  }

  protected def delete(relativeUrl: String): Future[Unit] = {
    val deleteApiCall = (at: String) => createWsRequest(relativeUrl, at).delete()
    executeRequest(deleteApiCall).map(handleNoContentResponse)
  }

  protected def getResponse(relativeUrl: String): Future[WSResponse] = {
    val getApiCall = (at: String) => createWsRequest(relativeUrl, at).get()
    executeRequest(getApiCall)
  }

  protected def executeRequest(apiCall: String => Future[WSResponse]): Future[WSResponse] = {
    for {
      accessToken <- accessTokenHandler.accessToken
      response <- apiCall(accessToken)
    } yield response
  }

  protected def createWsRequest(relativeUrl: String, accessToken: String): WSRequest = {
    val url = s"${configUtils.baseUrl}/$relativeUrl"
    log.debug(s"Executing API $url")
    wsClient
      .url(url)
      .addHttpHeaders("Authorization" -> s"Bearer $accessToken")
  }

  protected def handleJsonResponse(response: WSResponse): JsValue = {
    response.status match {
      case OK | CREATED | NO_CONTENT => response.json
      case _                         => ErrorResponseHandler.handle(response)
    }
  }

  protected def handleNoContentResponse(response: WSResponse): Unit = {
    response.status match {
      case OK | NO_CONTENT =>
      case _               => ErrorResponseHandler.handle(response)
    }
  }

}
