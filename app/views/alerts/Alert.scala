package views.alerts

trait Alert {
  def level: String
  def label: String
  def text: String
}

case class Success(label: String, text: String) extends Alert {
  def level = "success"
}
case class Info(label: String, text: String) extends Alert {
  def level = "info"
}
case class Warning(label: String, text: String) extends Alert {
  def level = "warning"
}
case class Danger(label: String, text: String) extends Alert {
  def level = "danger"
}
