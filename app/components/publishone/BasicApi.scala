package components.publishone

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.HttpMethods._
import components.common.ErrorResponseHandler
import play.api.Logger
import play.api.http.Status.{CREATED, NO_CONTENT, OK}
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.ws.ahc.AhcCurlRequestLogger
import play.api.libs.ws.{BodyWritable, WSClient, WSRequest, WSResponse}
import util.ConfigUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/** This class contains basic methods for Rest communication with PublishOne
  *
  * @param configUtils
  *   configuration
  * @param wsClient
  *   web client
  * @param accessTokenHandler
  *   access token handler
  */
abstract class BasicApi(configUtils: ConfigUtils, wsClient: WSClient, accessTokenHandler: AccessTokenHandler) {

  private lazy val log = Logger(getClass)

  protected def post[T: BodyWritable](relativeUrl: String, headers: Seq[(String, String)] = Seq.empty, requestBody: T): Future[WSResponse] = {
    val wsRequest = (at: String) =>
      createWsRequest(POST, relativeUrl, at)
        .addHttpHeaders(headers: _*)
        .withBody(requestBody)
    executeRequest(wsRequest)
  }

  protected def postJson(relativeUrl: String, requestBody: JsObject): Future[JsValue] = {
    post(relativeUrl, Seq("Content-Type" -> "application/json"), requestBody)
      .map(handleJsonResponse)
  }

  protected def put(relativeUrl: String, headers: Seq[(String, String)] = Seq.empty, requestBody: String): Future[WSResponse] = {
    val wsRequest = (at: String) =>
      createWsRequest(PUT, relativeUrl, at)
        .addHttpHeaders(headers: _*)
        .withBody(requestBody)
    executeRequest(wsRequest)
  }

  protected def putXml(relativeUrl: String, requestBody: String): Future[Unit] = {
    put(relativeUrl, Seq("Content-Type" -> "application/xml"), requestBody)
      .map(handleNoContentResponse)
  }

  protected def get(relativeUrl: String, headers: Seq[(String, String)] = Seq.empty): Future[WSResponse] = {
    val wsRequest = (at: String) => createWsRequest(GET, relativeUrl, at).addHttpHeaders(headers: _*)
    executeRequest(wsRequest)
  }

  protected def getJson(relativeUrl: String): Future[JsValue] = {
    get(relativeUrl, Seq("Accept" -> "application/json")).map(handleJsonResponse)
  }

  protected def delete(relativeUrl: String): Future[Unit] = {
    val wsRequest = (at: String) => createWsRequest(DELETE, relativeUrl, at)
    executeRequest(wsRequest).map(handleNoContentResponse)
  }

  protected def executeRequest(wsRequest: String => WSRequest): Future[WSResponse] = {
    for {
      accessToken <- accessTokenHandler.accessToken
      response <- wsRequest(accessToken).execute()
    } yield response
  }

  protected def createWsRequest(httpMethod: HttpMethod, relativeUrl: String, accessToken: String): WSRequest = {
    val url = s"${configUtils.publishOneUrl}/$relativeUrl"
    log.trace(s"Executing API $httpMethod $url")
    val wsRequest = wsClient
      .url(url)
      .withMethod(httpMethod.value)
      .addHttpHeaders("Authorization" -> s"Bearer $accessToken")
    if (configUtils.wsClientLogRequestEnabled) wsRequest.withRequestFilter(AhcCurlRequestLogger())
    else wsRequest
  }

  protected def handleJsonResponse(response: WSResponse): JsValue = {
    response.status match {
      case OK | CREATED => response.json
      case _            => ErrorResponseHandler.handle(response)
    }
  }

  protected def handleNoContentResponse(response: WSResponse): Unit = {
    response.status match {
      case OK | NO_CONTENT =>
      case _               => ErrorResponseHandler.handle(response)
    }
  }

}
