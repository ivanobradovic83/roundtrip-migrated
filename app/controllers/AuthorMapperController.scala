package controllers

import org.webjars.play.WebJarsUtil
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import play.api.{Configuration, Logger}
import service.authormapper.AuthorMapperService
import views.alerts.{Alert, Warning, Success => SucessAlert}

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

  def index: Action[AnyContent] = Action {
    Ok(views.html.mapAuthors(environment, swsBaseUrl, defaultContentVersion))
  }

  def map: Action[Map[String, Seq[String]]] = Action(parse.formUrlEncoded) { implicit request =>
    val query = request.body("query").headOption.filter(_.trim.nonEmpty)
    validateQuery(query) match {
      case Some(warning) => badRequestResponse(warning)
      case None          => startMapper(s"?${query.get}&order=documentFormat")
    }
  }

  private def validateQuery(query: Option[String]): Option[Alert] = {
    query match {
      case None                              => Option(Warning("Query", "Query cannot be empty!"))
      case Some(q) if q.contains("order=")   => Option(Warning("Query", "Query cannot contain order parameter!"))
      case Some(q) if q.contains("version=") => Option(Warning("Query", "Query cannot contain version parameter!"))
      case _                                 => None
    }
  }

  private def badRequestResponse(queryValidation: Alert) = {
    BadRequest(views.html.index(environment, swsBaseUrl, defaultContentVersion, Seq(queryValidation)))
  }

  private def startMapper(query: String) = {
    authorMapperService.map(query, false)
    val messages = Seq(SucessAlert("Author mapper", "Author mapper started successfully!"))
    Ok(views.html.mapAuthors(environment, swsBaseUrl, defaultContentVersion, messages))
  }

}
