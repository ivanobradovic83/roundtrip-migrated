package helpers

import play.api.libs.json.{JsValue, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import scala.util.{Try, Using}

object TestUtils {

  def mockedJsonResp(fileName: String): Future[JsValue] = Future {
    val filePath = s"test/resources/response/$fileName.json"
    val tryString: Try[String] = Using(Source.fromFile(filePath))(_.mkString)
    Json.parse(tryString.getOrElse(""))
  }

  def emptyJson: Future[JsValue] = Future.successful(Json.parse("{}"))

}
