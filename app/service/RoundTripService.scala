package service

import components.publishone.FolderApi
import components.sws.SwsApi
import dto.RoundTripDto
import play.api.Logger
import play.api.libs.json.JsValue

import java.io.ByteArrayInputStream
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class RoundTripService @Inject()(swsClient: SwsApi, publishOneImportService: PublishOneImportService, folderApi: FolderApi) {

  private lazy val log = Logger(getClass)

  def roundTrip(roundTripDto: RoundTripDto): Future[Unit] = Future {
    log.info(s"${roundTripDto.toString} started")
    roundTripFlow(roundTripDto)
      .onComplete(result => onRoundTripComplete(result, roundTripDto))
  }

  private def roundTripFlow(roundTripDto: RoundTripDto) = {
    for {
      (xhtml, metaXml) <- swsClient.getXhtml(roundTripDto.docKey) zip swsClient.getMetaXml(roundTripDto.docKey)
      publishOneDoc <- applyTransformation(roundTripDto, xhtml, metaXml)
      publishOneDocId <- publishOneImportService.importDocument(roundTripDto, publishOneDoc)
      status <- triggerPublishOneDocPublish(roundTripDto, publishOneDocId)
    } yield status
  }

  private def onRoundTripComplete[A](result: Try[A], roundTripDto: RoundTripDto): Unit = result match {
    case Failure(exception) => log.error(s"${roundTripDto.toString} failed", exception)
    case Success(_)         => log.info(s"${roundTripDto.toString} done successfully")
  }

  private def applyTransformation(roundTripDto: RoundTripDto, xhtml: Array[Byte], metaXml: Array[Byte]): Future[Array[Byte]] = {
    log.info(s"${roundTripDto.toString} XSL transformation started")

    println("----------------------------------------")
//    val str = new String(metaXml, StandardCharsets.UTF_8)
//    println(str)

    val atom = xml.XML.load(new ByteArrayInputStream(metaXml))
    val metaTitle = (atom \ "legalArea" \ "title").headOption.get.text
    log.info(s"Meta title: $metaTitle")

    val xhtmlAtom = xml.XML.load(new ByteArrayInputStream(xhtml))
    val xhtmlTitle = (xhtmlAtom \ "body" \ "section" \ "h1").headOption.get.text
    log.info(s"XHTML title: $xhtmlTitle")
    println("----------------------------------------")

    log.info(s"${roundTripDto.toString} XSL transformation ended")
    Future.successful(xhtml)
  }

  private def triggerPublishOneDocPublish(roundTripDto: RoundTripDto, publishOneDocId: Int): Future[Unit] = {
    log.info(s"${roundTripDto.toString} PublishOne publish trigger started")

    folderApi
      .getFolderById(roundTripDto.destination.toInt)
      .flatMap(response => {
        printPublishOneFolderDetails(response)
        log.info(s"${roundTripDto.toString} PublishOne publish trigger ended")
        Future.successful()
      })
  }

  private def printPublishOneFolderDetails(response: JsValue) = {
    val folderId = response \ "id"
    val folderName = response \ "name"
    println(s"folderId: $folderId  folderName: $folderName")
  }
}
