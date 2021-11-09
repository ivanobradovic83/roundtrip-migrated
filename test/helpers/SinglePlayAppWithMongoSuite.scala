package helpers

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder

/**
  * A suite that provides support of a single play app server with a Mongo test suite.
  */
abstract class SinglePlayAppWithMongoSuite extends PlaySpec with GuiceOneServerPerSuite {

  val reactiveMongoRetries = 60

  def additionalConfiguration: Map[String, Any] = Map.empty

  val builder: GuiceApplicationBuilder = new GuiceApplicationBuilder()
    .configure(additionalConfiguration)

  override implicit lazy val app = builder.build()

}
