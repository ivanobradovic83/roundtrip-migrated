package controllers.validation

import service.common.monithoring.InProgressHandler
import util.ConfigUtils
import views.alerts.{Alert, Danger, Warning}

import java.io.File
import javax.inject.{Inject, Singleton}

@Singleton
class ControllerValidation @Inject()(configUtils: ConfigUtils, inProgressHandler: InProgressHandler) {

  def validateQueryAndProcessInProgress(query: String): Seq[Alert] =
    filterExistingAlerts(validateQuery(query), validateProcessInProgress())

  def validateAuthorMappingFileExistAndProcessInProgress(): Seq[Alert] =
    filterExistingAlerts(validateAuthorMappingFileExist(), validateProcessInProgress())

  def validateQuery(query: String): Option[Alert] = query match {
    case ""                          => Option(Warning("Query", "Query cannot be empty!"))
    case q if q.contains("order=")   => Option(Warning("Query", "Query cannot contain order parameter!"))
    case q if q.contains("version=") => Option(Warning("Query", "Query cannot contain version parameter!"))
    case _                           => None
  }

  def validateProcessInProgress(): Option[Alert] = inProgressHandler.getProcessInProgress match {
    case Some(processInProgressName) => Option(Warning("Warning", s"Process $processInProgressName is already in progress!"))
    case _                           => None
  }

  def validateAuthorMappingFileExist(): Option[Alert] =
    if (new File(configUtils.publishOneAuthorMappingFile).exists()) None
    else Option(Danger("Error", s"File ${configUtils.publishOneAuthorMappingFile} does not exist!"))

  def filterExistingAlerts(alerts: Option[Alert]*): Seq[Alert] = alerts.filter(_.isDefined).flatten

}
