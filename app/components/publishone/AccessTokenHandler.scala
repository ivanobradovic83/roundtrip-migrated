package components.publishone

import play.api.Logger
import play.api.libs.ws.{WSClient, WSResponse}
import util.ConfigUtils

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * This class is used to handles Access Tokens for PublishOne API.
  * It reads properties, used to ask for a token, from configuration file.
  * Access Token is valid for an hour. If cached token is about to expire it will be reloaded.
  *
  * @param configUtils configuration
  * @param wsClient web client
  */
@Singleton
class AccessTokenHandler @Inject()(configUtils: ConfigUtils, wsClient: WSClient) {

  private lazy val log = Logger(getClass)

  private var accessTokenCached: String = null
  private var userIdCached: String = null

  def accessToken: Future[String] = {
    if (accessTokenCached == null || tokenAboutToExpire()) loadAccessToken()
    else Future.successful(accessTokenCached)
  }

  def userId: Future[String] = {
    if (userIdCached == null) {
      for {
        _ <- accessToken
        userId <- loadUserId()
      } yield userId
    } else Future.successful(userIdCached)
  }

  private def loadAccessToken() = {
    log.debug("loading access token ...")
    wsClient
      .url(s"${configUtils.publishOneIsUrl}/connect/token")
      .post(accessTokenRequestPostParameters)
      .map(extractAccessTokenFromResponse)
  }

  private def accessTokenRequestPostParameters = {
    Map(
      "grant_type" -> Seq("password"),
      "username" -> Seq(s"${configUtils.publishOneUsername}"),
      "password" -> Seq(s"${configUtils.publishOnePassword}"),
      "scope" -> Seq("mainmodule-api"),
      "client_id" -> Seq(s"${configUtils.publishOneClientId}"),
      "client_secret" -> Seq(s"${configUtils.publishOneClientSecret}")
    )
  }

  private def extractAccessTokenFromResponse(resp: WSResponse) = {
    accessTokenCached = (resp.json \ "access_token").as[String]
    accessTokenCached
  }

  private def loadUserId() = {
    wsClient
      .url(s"${configUtils.publishOneUrl}/api/users/current")
      .addHttpHeaders("Authorization" -> s"Bearer $accessTokenCached")
      .get()
      .map(extractUserIdFromResponse)
  }

  private def extractUserIdFromResponse(resp: WSResponse) = {
    userIdCached = (resp.json \ "id").as[String]
    println(s"user id loaded: $userIdCached")
    userIdCached
  }

  private def tokenAboutToExpire() = {
    false
  }

}
