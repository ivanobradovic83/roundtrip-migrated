package controllers.validation

import service.common.monithoring.InProgressHandler
import views.alerts.{Alert, Warning}

object ControllerValidation {

  def validateQueryAndProcessInProgress(query: String): Seq[Alert] =
    filterExistingAlerts(validateQuery(query), validateProcessInProgress())

  def validateQuery(query: String): Option[Alert] = query match {
    case ""                          => Option(Warning("Query", "Query cannot be empty!"))
    case q if q.contains("order=")   => Option(Warning("Query", "Query cannot contain order parameter!"))
    case q if q.contains("version=") => Option(Warning("Query", "Query cannot contain version parameter!"))
    case _                           => None
  }

//  def validateProcessInProgress(): Option[Alert] = InProgressHandler.getProcessInProgress match {
//    case Some(processInProgressName) => Option(Warning("Warning", s"Process $processInProgressName is already in progress!"))
//    case _                           => None
//  }

  def validateProcessInProgress(): Option[Alert] = None

  def filterExistingAlerts(alerts: Option[Alert]*): Seq[Alert] = alerts.filter(_.isDefined).flatten

}
