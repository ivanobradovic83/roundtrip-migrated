package components.sws

import components.common.ErrorResponseHandler
import play.api.http.Status.OK
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * CWC SWS Api
  *
  * Contains methods to fetch document content or metadata.
  *
  * @param config configuration
  * @param wsClient web client
  */
class SwsApi @Inject()(config: Configuration, wsClient: WSClient) {

  private lazy val log = Logger(getClass)
  private lazy val baseUrl = config.get[String]("cwc.sws.url")

  def getXhtml(docKey: String): Future[Array[Byte]] = executeRequest(docKey, "xhtml")

  def getMetaXml(docKey: String): Future[Array[Byte]] = executeRequest(docKey, "xml")

  private def executeRequest(docKey: String, docType: String): Future[Array[Byte]] = {
    val url = s"$baseUrl/$docKey/$docKey.$docType"
    log.debug(s"Fetching document $url")
    wsClient
      .url(url)
      .get()
      .map(handleResponse)
  }

  protected def handleResponse(response: WSResponse): Array[Byte] = {
    response.status match {
      case OK => response.bodyAsBytes.toArray
      case _  => ErrorResponseHandler.handle(response)
    }
  }

}
