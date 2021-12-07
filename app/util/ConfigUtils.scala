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
  lazy val documentStateCreated: Int = config.get[Int]("publishOne.document.state.created")
  lazy val documentStatePublish: Int = config.get[Int]("publishOne.document.state.publish")
  lazy val publicationProfileOnlineZip: String = config.get[String]("publishOne.publication.profile.onlineZip")

  lazy val publishOneAuthorsRootFolderId: Int =
    Try("publishOne.authors.rootFolderId").map(config.get[Int]).getOrElse(-1)

  lazy val checkOperationStateMaxAttempts: Int =
    Try("publishOne.checkOperationState.maxAttempts").map(config.get[Int]).getOrElse(10)
  lazy val checkOperationStateDelay: Long =
    Try("publishOne.checkOperationState.delay").map(config.get[Long]).getOrElse(2000L)

  lazy val wsClientLogRequestEnabled: Boolean =
    Try("wsClient.log.request.enabled").map(config.get[Boolean]).getOrElse(false)

  lazy val parallelism: Int =
    Try("publishOne.flow.run.parallelism").map(config.get[Int]).getOrElse(4)

  lazy val publishOneAuthorMappingFile: String =
    Try("publishOne.author-mapping.file").map(config.get[String]).getOrElse("author-mapping.csv")
}
