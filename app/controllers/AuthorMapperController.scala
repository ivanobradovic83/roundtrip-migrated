package controllers

import org.webjars.play.WebJarsUtil
import play.api.data.Forms._
import play.api.data._
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import play.api.{Configuration, Logger}
import service.authormapper.AuthorMapperService
import controllers.validation.ControllerValidation._
import views.alerts.{Alert, Success => SucessAlert}

import java.io.File
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Controller which handles author mappings feature
  */
class AuthorMapperController @Inject()(config: Configuration, cc: ControllerComponents, authorMapperService: AuthorMapperService)(
    implicit webJarsUtil: WebJarsUtil)
    extends AbstractController(cc) {

  lazy val environment: String = config.get[String]("cwc.environment")
  lazy val swsBaseUrl: String = config.get[String]("cwc.sws.url")
  lazy val defaultContentVersion: Int = config.get[Int]("cwc.sws.contentVersion")
  lazy val log: Logger = Logger(getClass)

  private val mapAuthorsForm: Form[(String, Boolean)] = Form(
    tuple(
      "query" -> text,
      "createMissingDocuments" -> default(boolean, false)
    ))

  def index: Action[AnyContent] = Action {
    Ok(views.html.mapAuthors(environment, swsBaseUrl, defaultContentVersion))
  }

  def map: Action[Map[String, Seq[String]]] = Action(parse.formUrlEncoded) { implicit request =>
    val (query, createMissingDocuments) = mapAuthorsForm.bindFromRequest().get
    validateQueryAndProcessInProgress(query) match {
      case alerts if alerts.nonEmpty => badRequestResponse(alerts)
      case _                         => startMapper(query, createMissingDocuments)
    }
  }

  def downloadMapping: Action[AnyContent] = Action {
    validateProcessInProgress() match {
      case Some(alert) => badRequestResponse(Seq(alert))
      case _           => Ok.sendFile(content = new File("./author-mapping.csv"), fileName = _ => Some("author-mapping.csv"))
    }
  }

  private def badRequestResponse(validationAlerts: Seq[Alert]) =
    BadRequest(views.html.mapAuthors(environment, swsBaseUrl, defaultContentVersion, validationAlerts))

  private def startMapper(query: String, createMissingDocuments: Boolean) = {
    authorMapperService.map(s"?$query&order=documentFormat", createMissingDocuments)
    val messages = Seq(SucessAlert("Author mapper", "Author mapper started successfully!"))
    Ok(views.html.mapAuthors(environment, swsBaseUrl, defaultContentVersion, messages))
  }

}
