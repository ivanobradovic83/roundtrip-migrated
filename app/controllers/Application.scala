package controllers

import nl.sdu.cwc.common.model.StatusReport
import org.webjars.play.WebJarsUtil
import play.api.libs.json._
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import play.api.{Configuration, Logger}

import javax.inject.Inject

class Application @Inject()(config: Configuration, cc: ControllerComponents)(implicit webJarsUtil: WebJarsUtil) extends AbstractController(cc) {

  lazy val environment: String = config.get[String]("cwc.environment")
  lazy val swsBaseUrl: String = config.get[String]("cwc.sws.url")
  lazy val defaultContentVersion: Int = config.get[Int]("cwc.sws.contentVersion")
  lazy val log: Logger = Logger(getClass)

  def status: Action[AnyContent] = Action { implicit request =>
    val statusReport = StatusReport("CWC Round-trip PublishOne", build.BuildInfo.version, operational = true, Some("Running"))
    Ok(Json.toJson(statusReport))
  }

  def uploadFile: Action[AnyContent] = Action {
    Ok(views.html.uploadFile(environment, swsBaseUrl, defaultContentVersion))
  }

}
