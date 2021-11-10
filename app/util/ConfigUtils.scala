package util

import play.api.Configuration

import javax.inject.{Inject, Singleton}
import scala.util.Try

@Singleton
class ConfigUtils @Inject()(config: Configuration) {

  lazy val baseUrl: String = config.get[String]("publishOne.url")

  lazy val checkOperationStateMaxAttempts: Int =
    Try("publishOne.checkOperationState.maxAttempts").map(config.get[Int]).getOrElse(5)
  lazy val checkOperationStateDelay: Long =
    Try("publishOne.checkOperationState.delay").map(config.get[Long]).getOrElse(1000L)

}
