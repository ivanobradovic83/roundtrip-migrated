package components.common

import play.api.Logger
import play.api.libs.ws.WSResponse

object ErrorResponseHandler {

  private lazy val log = Logger(getClass)

  def handle(response: WSResponse): Nothing = {
    val message =
      s"\nError while executing ${response.uri}\nResponse status: ${response.status} ${response.statusText}\nResponse content: ${response.body}"
    log.error(message)
    throw new Exception(message)
  }

}
