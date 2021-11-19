package components.sws

import java.io.ByteArrayInputStream

import akka.stream.scaladsl.Source
import javax.inject.Inject
import play.api.http.Status.OK
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}

import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.Elem

class SwsSourceApi @Inject()(val config: Configuration, wsClient: WSClient) {

  private lazy val log = Logger(getClass)
  private lazy val baseUrl = config.get[String]("cwc.sws.url")

  def searchAndStreamDocs(relativeUrl: String): Source[String, _] = {

    val keyExtractor = """^.*/([^\?]+).*$""".r

    case class PageResult(maybeNextUrl: Option[String], docKeys: immutable.Seq[String])

    def toDocKeys(atom: Elem): immutable.Seq[String] = {
      (atom \ "entry").map { e =>
        (e \ "id").headOption.map(_.text match { case keyExtractor(v) => v }).get
      }
    }

    def fetchNextPage(pageUrl: String): Future[PageResult] = requestAbsolute(pageUrl).map {
      case Right(atomBytes) =>
        log.debug(s"Obtained sws page: $pageUrl")
        val atom = xml.XML.load(new ByteArrayInputStream(atomBytes))
        PageResult(maybeNextUrl = atom.\("link").find(_.\@("rel") == "next").map(_ \@ "href"), docKeys = toDocKeys(atom))
      case Left(err) => throw new Error(s"Cannot retrieve SWS feed page. Cause: $err")
    }

    val pageSource = Source.unfoldAsync(Option(s"$baseUrl/$relativeUrl") -> immutable.Seq.empty[String]) {
      case (Some(url), _) =>
        fetchNextPage(url).map {
          case PageResult(_, Nil)          => None
          case PageResult(maybeNext, keys) => Option((maybeNext -> keys) -> keys)
        }
      case (None, _) => Future.successful(None)
    }

    pageSource.flatMapConcat(Source(_))
  }

  private def requestAbsolute(url: String): Future[Either[String, Array[Byte]]] = {
    wsClient
      .url(url)
      .get()
      .map { response =>
        response.status match {
          case OK => Right(response.bodyAsBytes.toArray)
          case _  => Left(s"While fetching $url: HTTP status: ${response.statusText}")
        }
      }
      .recoverWith({
        case e: Exception =>
          log.error(s"Unable to connect to $url. Reason: ${e.getMessage}", e)
          Future.successful(Left(s"While fetching $url: Error: ${e.getMessage}"))
      })
  }

}
