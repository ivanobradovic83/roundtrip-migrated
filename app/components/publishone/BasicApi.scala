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
        .map(handleJsonResponse)
    executeRequest(putApiCall)
  }

  protected def putXml(relativeUrl: String, requestBody: String): Future[WSResponse] = {
    val putApiCall = (at: String) =>
      createWsRequest(relativeUrl, at)
        .addHttpHeaders("Content-Type" -> "application/xml")
        .put(requestBody)
//        .map(handleJsonResponse)
    for {
      accessToken <- accessTokenHandler.accessToken
      response <- putApiCall(accessToken)
    } yield response
  }

  protected def getJson(relativeUrl: String): Future[JsValue] = {
    val getApiCall = (at: String) =>
      createWsRequest(relativeUrl, at)
        .addHttpHeaders("Accept" -> "application/json")
        .get()
        .map(handleJsonResponse)
    executeRequest(getApiCall)
  }

  protected def getResponse(relativeUrl: String): Future[WSResponse] = {
    val getApiCall = (at: String) => createWsRequest(relativeUrl, at).get()
    for {
      accessToken <- accessTokenHandler.accessToken
      response <- getApiCall(accessToken)
    } yield response
  }

  protected def executeRequest(apiCall: String => Future[JsValue]): Future[JsValue] = {
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

}
