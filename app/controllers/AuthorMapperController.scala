package controllers

import org.webjars.play.WebJarsUtil
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import play.api.{Configuration, Logger}
import service.authormapper.AuthorMapperService
import views.alerts.{Alert, Warning, Success => SucessAlert}

import play.api.data._
import play.api.data.Forms._

import javax.inject.Inject

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

  val mapAuthorsForm: Form[(String, Boolean)] = Form(
    tuple(
      "query" -> text,
      "createMissingDocuments" -> default(boolean, false)
    ))

  def index: Action[AnyContent] = Action {
    Ok(views.html.mapAuthors(environment, swsBaseUrl, defaultContentVersion))
  }

  def map: Action[Map[String, Seq[String]]] = Action(parse.formUrlEncoded) { implicit request =>
    val (query, createMissingDocuments) = mapAuthorsForm.bindFromRequest().get
    if(authorMapperService.getIsMappingInProgress)
      badRequestResponse(Warning("Warning", "Mapping already in progress"))
    else
      validateQuery(query) match {
        case Some(warning) => badRequestResponse(warning)
        case None          => startMapper(s"?$query&order=documentFormat", createMissingDocuments)
      }

  }

  private def validateQuery(query: String): Option[Alert] = {
    query match {
      case ""                          => Option(Warning("Query", "Query cannot be empty!"))
      case q if q.contains("order=")   => Option(Warning("Query", "Query cannot contain order parameter!"))
      case q if q.contains("version=") => Option(Warning("Query", "Query cannot contain version parameter!"))
      case _                           => None
    }
  }

  private def badRequestResponse(queryValidation: Alert) = {
    BadRequest(views.html.index(environment, swsBaseUrl, defaultContentVersion, Seq(queryValidation)))
  }

  private def startMapper(query: String, createMissingDocuments: Boolean) = {
    authorMapperService.map(query, createMissingDocuments)
    val messages = Seq(SucessAlert("Author mapper", "Author mapper started successfully!"))
    Ok(views.html.mapAuthors(environment, swsBaseUrl, defaultContentVersion, messages))
  }

}
