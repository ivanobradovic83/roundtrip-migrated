package service.common.monithoring

import javax.inject.{Inject, Singleton}

/**
  * This class handles process in progress status. Only one process can be started at the time
  * (regardless if it is Round-trip or Authors mapping process)
  *
  * Whenever a process is started it has to invoke startProcess()
  * and when it is ended it has to invoke stopProcess()
  */
@Singleton
class InProgressHandler @Inject()() {

  private var processInProgress: Option[String] = None

  def startProcess(processName: String): Unit = this.synchronized {
    processInProgress match {
      case Some(processInProgressName) => throw new Exception(s"$processInProgressName already in progress")
      case None                        => processInProgress = Some(processName)
    }
  }

  def stopProcess(processName: String): Unit = this.synchronized {
    processInProgress match {
      case Some(processInProgressName) if processName == processInProgressName => processInProgress = None
      case None                                                                => throw new Exception("There is no process in progress")
      case Some(processInProgressName)                                         => throw new Exception(s"$processInProgressName already in progress")
    }
  }

  def getProcessInProgress: Option[String] = processInProgress

}
