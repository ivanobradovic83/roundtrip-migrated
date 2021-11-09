package components.common

import play.api.libs.ws.WSResponse

object ErrorResponseHandler {

  def handle(response: WSResponse): Nothing = {
    throw new Exception(
      s"\nError while executing ${response.uri}"
        + s"\nResponse status: ${response.status} ${response.statusText}"
        + s"\nResponse content: ${response.body}"
    )
  }

}
