package controllers

import build.BuildInfo
import helpers.SinglePlayAppWithMongoSuite
import org.scalatest.BeforeAndAfterAll
import play.api.http.MimeTypes
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test._

class ApplicationSpec extends SinglePlayAppWithMongoSuite with BeforeAndAfterAll {

  override def additionalConfiguration: Map[String, Any] = Map("cwc.sws.contentVersion" -> 3)

  "Application" should {

    "send 404 on a bad request" in {
      route(app, FakeRequest(GET, "/boum")).map(status(_)) mustBe Some(NOT_FOUND)
    }

    "render the index page, listing the version number" in {
      val home = route(app, FakeRequest(GET, "/")).get

      status(home) mustEqual OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("sdu-cwc-roundtrip-publishone " + BuildInfo.version)
    }

    "report everything is OK" in {
      val result = route(app, FakeRequest(GET, "/status")).get

      status(result) mustEqual OK
      contentType(result) mustBe Some(MimeTypes.JSON)
      contentAsJson(result) mustEqual Json.obj("name" -> "CWC Round-trip PublishOne",
                                               "version" -> build.BuildInfo.version,
                                               "operational" -> true,
                                               "message" -> "Running")
    }
  }

}
