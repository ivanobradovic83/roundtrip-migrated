package components.publishone

import components.common.ErrorResponseHandler
import play.api.http.Status.{CREATED, NO_CONTENT, OK}
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * This class contains basic methods for Rest communication with PublishOne
  *
  * @param config configuration
  * @param wsClient web client
  * @param accessTokenHandler access token handler
  */
abstract class BasicApi(config: Configuration, wsClient: WSClient, accessTokenHandler: AccessTokenHandler) {

  private lazy val baseUrl = config.get[String]("publishOne.url")
  private lazy val log = Logger(getClass)

  protected def postJson(relativeUrl: String, requestBody: JsObject): Future[JsValue] = {
    val putApiCall = (at: String) =>
      createWsRequest(relativeUrl, at)
        .addHttpHeaders("Content-Type" -> "application/json")
        .post(requestBody)
        .map(handleJsonResponse)
    executeRequest(putApiCall)
  }

  protected def putXml(relativeUrl: String, requestBody: String): Future[JsValue] = {
    val putApiCall = (at: String) =>
      createWsRequest(relativeUrl, at)
        .addHttpHeaders("Content-Type" -> "application/xml")
        .put(requestBody)
        .map(handleJsonResponse)
    executeRequest(putApiCall)
  }

  protected def getJson(relativeUrl: String): Future[JsValue] = {
    val getApiCall = (at: String) =>
      createWsRequest(relativeUrl, at)
        .addHttpHeaders("Accept" -> "application/json")
        .get()
        .map(handleJsonResponse)
    executeRequest(getApiCall)
  }

  protected def executeRequest(apiCall: String => Future[JsValue]): Future[JsValue] = {
    for {
      accessToken <- accessTokenHandler.accessToken
      response <- apiCall(accessToken)
    } yield response
  }

  protected def createWsRequest(relativeUrl: String, accessToken: String): WSRequest = {
    val url = s"$baseUrl/$relativeUrl"
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
