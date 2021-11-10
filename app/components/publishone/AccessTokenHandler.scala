package components.publishone

import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * This class is used to handles Access Tokens for PublishOne API.
  * It reads properties, used to ask for a token, from configuration file.
  * Access Token is valid for an hour. If cached token is about to expire it will be reloaded.
  */
@Singleton
class AccessTokenHandler @Inject()(config: Configuration, ws: WSClient) {

  private lazy val publishOneIsUrl: String = config.get[String]("publishOne.is.url")
  private lazy val publishOneUrl = config.get[String]("publishOne.url")
  private lazy val publishOneUsername: String = config.get[String]("publishOne.username")
  private lazy val publishOnePassword: String = config.get[String]("publishOne.password")
  private lazy val publishOneClientId: String = config.get[String]("publishOne.clientId")
  private lazy val publishOneClientSecret: String = config.get[String]("publishOne.clientSecret")
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
    ws.url(s"$publishOneIsUrl/connect/token")
      .post(accessTokenRequestPostParameters)
      .map(extractAccessTokenFromResponse)
  }

  private def accessTokenRequestPostParameters = {
    Map(
      "grant_type" -> Seq("password"),
      "username" -> Seq(s"$publishOneUsername"),
      "password" -> Seq(s"$publishOnePassword"),
      "scope" -> Seq("mainmodule-api"),
      "client_id" -> Seq(s"$publishOneClientId"),
      "client_secret" -> Seq(s"$publishOneClientSecret")
    )
  }

  private def extractAccessTokenFromResponse(resp: WSResponse) = {
    accessTokenCached = (resp.json \ "access_token").as[String]
    accessTokenCached
  }

  private def loadUserId() = {
    ws.url(s"$publishOneUrl/api/users/current")
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
