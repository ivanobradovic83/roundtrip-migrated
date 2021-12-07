package service.common.monithoring

import helpers.ScalaSpec

class InProgressHandlerSpec extends ScalaSpec {

  lazy val processName1 = "process1"
  lazy val processName2 = "process2"

  val cut = new InProgressHandler()

  override def beforeEach(): Unit =
    cut.getProcessInProgress match {
      case Some(processName) => cut.stopProcess(processName)
      case None              =>
    }

  "when new process started then it is successfully stopped" in {
    cut.getProcessInProgress should be(None)

    cut.startProcess(processName1)
    cut.getProcessInProgress should be(Some(processName1))

    cut.stopProcess(processName1)
    cut.getProcessInProgress should be(None)
  }

  "when process is started then new process cannot be started" in {
    cut.getProcessInProgress should be(None)
    cut.startProcess(processName1)
    cut.getProcessInProgress should be(Some(processName1))

    val exception = the[Exception] thrownBy cut.startProcess(processName2)
    exception.getMessage should be(s"$processName1 already in progress")
  }

  "when process is not started then it cannot be stopped" in {
    cut.getProcessInProgress should be(None)

    val exception = the[Exception] thrownBy cut.stopProcess(processName2)
    exception.getMessage should be("There is no process in progress")
  }

  "when one process is started then another process cannot be stopped" in {
    cut.getProcessInProgress should be(None)
    cut.startProcess(processName1)
    cut.getProcessInProgress should be(Some(processName1))

    val exception = the[Exception] thrownBy cut.stopProcess(processName2)
    exception.getMessage should be(s"$processName1 already in progress")
  }

}
