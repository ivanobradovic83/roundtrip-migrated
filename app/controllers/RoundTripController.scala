package controllers

import dto.RoundTripDto
import org.webjars.play.WebJarsUtil
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import play.api.{Configuration, Logger}
import service.roundtrip.RoundTripService
import views.alerts.{Alert, Warning, Success => SucessAlert}

import java.util.UUID
import javax.inject.Inject

/**
  * Controller which handles round-trip by query feature
  */
class RoundTripController @Inject()(config: Configuration, cc: ControllerComponents, roundTripService: RoundTripService)(
    implicit webJarsUtil: WebJarsUtil)
    extends AbstractController(cc) {

  lazy val environment: String = config.get[String]("cwc.environment")
  lazy val swsBaseUrl: String = config.get[String]("cwc.sws.url")
  lazy val defaultContentVersion: Int = config.get[Int]("cwc.sws.contentVersion")
  lazy val log: Logger = Logger(getClass)

  def index: Action[AnyContent] = Action {
    Ok(views.html.index(environment, swsBaseUrl, defaultContentVersion))
  }

  def roundTripByQuery: Action[Map[String, Seq[String]]] = Action(parse.formUrlEncoded) { implicit request =>
    val query = request.body("query").headOption.filter(_.trim.nonEmpty)
    val documentType = request.body("documentType").headOption.getOrElse("")
    val destination = request.body("destination").headOption.filter(_.trim.nonEmpty)
    lazy val queryValidation = validateQuery(query)
    lazy val destinationValidation = validateDestination(destination)

    if (queryValidation.isDefined) badRequestResponse(queryValidation)
    else if (destinationValidation.isDefined) badRequestResponse(destinationValidation)
    else startRoundTrip(query, documentType, destination)
  }

  private def startRoundTrip(query: Option[String], docType: String, destination: Option[String]) = {
    roundTripService.roundTrip(RoundTripDto(UUID.randomUUID.toString, query.get, docType, destination.get))
    Ok(views.html.index(environment, swsBaseUrl, defaultContentVersion, Seq(SucessAlert("Round-trip", "Round-trip started successfully!"))))
  }

  private def badRequestResponse(queryValidation: Option[Alert]) = {
    BadRequest(views.html.index(environment, swsBaseUrl, defaultContentVersion, Seq(queryValidation.get)))
  }

  private def validateQuery(query: Option[String]): Option[Alert] = {
    query match {
      case None                              => Option(Warning("Query", "Query cannot be empty!"))
      case Some(q) if q.contains("order=")   => Option(Warning("Query", "Query cannot contain order parameter!"))
      case Some(q) if q.contains("version=") => Option(Warning("Query", "Query cannot contain version parameter!"))
      case _                                 => None
    }
  }

  private def validateDestination(destination: Option[String]): Option[Alert] = {
    destination match {
      case None       => Option(Warning("Destination", "Destination cannot be empty!"))
      case Some(dest) => validateDestinationAtPublishOne(dest)
    }
  }

  private def validateDestinationAtPublishOne(destination: String): Option[Alert] = {
    // p1Client.getFolder(destination)
    // if destination exists return None
    // if destination does not exists return Option(Warning("Destination", "Destination $destination does not exist at PublishOne!"))
    None
  }

}
