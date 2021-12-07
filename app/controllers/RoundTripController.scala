package controllers

import controllers.validation.ControllerValidation._
import dto.RoundTripDto
import org.webjars.play.WebJarsUtil
import play.api.data.{Form, FormError}
import play.api.data.Forms.{of, text, tuple}
import play.api.data.format.Formatter
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import play.api.{Configuration, Logger}
import service.roundtrip.RoundTripService
import util.RoundTripActions
import util.RoundTripActions.RoundTripAction
import views.alerts.{Alert, Success => SucessAlert}

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

  implicit def matchFilterFormat: Formatter[RoundTripAction] = new Formatter[RoundTripAction] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], RoundTripActions.Value] =
      data
        .get(key)
        .map(RoundTripActions.withName)
        .toRight(Seq(FormError(key, "error.required", Nil)))

    override def unbind(key: String, value: RoundTripAction) =
      Map(key -> value.toString)
  }

  private val roundTripForm: Form[(String, String, String, RoundTripAction)] = Form(
    tuple(
      "query" -> text,
      "documentType" -> text,
      "destination" -> text,
      "action" -> of[RoundTripAction]
    ))

  def index: Action[AnyContent] = Action {
    Ok(views.html.index(environment, swsBaseUrl, defaultContentVersion))
  }

  def roundTripByQuery: Action[Map[String, Seq[String]]] = Action(parse.formUrlEncoded) { implicit request =>
    val (query, documentType, destination, action) = roundTripForm.bindFromRequest().get
    validateRoundTrip(query, destination) match {
      case alerts if alerts.nonEmpty => badRequestResponse(alerts)
      case _                         => startRoundTrip(query, documentType, destination, action)
    }
  }

  private def startRoundTrip(query: String, docType: String, destination: String, action: RoundTripAction) = {
    roundTripService.roundTrip(RoundTripDto(UUID.randomUUID.toString, s"?$query&order=documentFormat", docType, destination, action))
    Ok(views.html.index(environment, swsBaseUrl, defaultContentVersion, Seq(SucessAlert("Round-trip", "Round-trip started successfully!"))))
  }

  private def badRequestResponse(validationAlerts: Seq[Alert]) =
    BadRequest(views.html.index(environment, swsBaseUrl, defaultContentVersion, validationAlerts))

  def validateRoundTrip(query: String, destination: String): Seq[Alert] =
    validateQueryAndProcessInProgress(query) ++ filterExistingAlerts(validateDestinationAtPublishOne(destination))

  private def validateDestinationAtPublishOne(destination: String): Option[Alert] = {
    // p1Client.getFolder(destination)
    // if destination exists return None
    // if destination does not exists return Option(Warning("Destination", "Destination $destination does not exist at PublishOne!"))
    None
  }

}
