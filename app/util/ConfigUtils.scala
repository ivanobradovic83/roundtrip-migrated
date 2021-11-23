package util

import play.api.Configuration

import javax.inject.{Inject, Singleton}
import scala.util.Try

@Singleton
class ConfigUtils @Inject()(config: Configuration) {

  lazy val publishOneIsUrl: String = config.get[String]("publishOne.is.url")
  lazy val publishOneUrl: String = config.get[String]("publishOne.url")
  lazy val publishOneUsername: String = config.get[String]("publishOne.username")
  lazy val publishOnePassword: String = config.get[String]("publishOne.password")
  lazy val publishOneClientId: String = config.get[String]("publishOne.clientId")
  lazy val publishOneClientSecret: String = config.get[String]("publishOne.clientSecret")

  lazy val publishOneAuthorsRootFolderId: Int = config.get[Int]("publishOne.authors.rootFolderId")

  lazy val checkOperationStateMaxAttempts: Int =
    Try("publishOne.checkOperationState.maxAttempts").map(config.get[Int]).getOrElse(5)
  lazy val checkOperationStateDelay: Long =
    Try("publishOne.checkOperationState.delay").map(config.get[Long]).getOrElse(1000L)

  lazy val wsClientLogRequestEnabled: Boolean =
    Try("wsClient.log.request.enabled").map(config.get[Boolean]).getOrElse(false)

}
