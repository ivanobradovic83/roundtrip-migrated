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

class RoundTripService @Inject()(swsClient: SwsApi, publishOneImportService: PublishOneImportService, folderApi: FolderApi,
                                 xmlTransformationService: XmlTransformationService) {

  private lazy val log = Logger(getClass)

  def roundTrip(roundTripDto: RoundTripDto): Future[Unit] = Future {
    log.info(s"${roundTripDto.toString} started")
    roundTripFlow(roundTripDto)
      .onComplete(result => onRoundTripComplete(result, roundTripDto))
  }

  private def roundTripFlow(roundTripDto: RoundTripDto) = {
    for {
      (xhtml, metaXml) <- swsClient.getXhtml(roundTripDto.docKey) zip swsClient.getMetaXml(roundTripDto.docKey)
      publishOneDocXml <- applyTransformation(roundTripDto, xhtml, metaXml)
      publishOneDocId <- publishOneImportService.importDocument(roundTripDto, publishOneDocXml)
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

    val publishoneDefaultFormat =
      <document> { xml.XML.load(new ByteArrayInputStream(xhtml)) } {xml.XML.load(new ByteArrayInputStream(metaXml))} </document>

    val voorwas = xmlTransformationService.transformDocument("/xslt/1-voorwas.xsl", publishoneDefaultFormat.toString().getBytes())
    val generiek = xmlTransformationService.transformDocument("/xslt/2-generiek.xsl", voorwas)
    val specifiek = xmlTransformationService.transformDocument("/xslt/3-specifiek.xsl", generiek)
    val metadata = xmlTransformationService.transformDocument("/xslt/4-metadata.xsl", specifiek)


    val documentNode = xml.XML.load(new ByteArrayInputStream(metadata)) \ "document"
    xml.XML.save("./transformed.xhtml", xml.XML.load(new ByteArrayInputStream(documentNode.toString().getBytes())))

    println("----------------------------------------")

    log.info(s"${roundTripDto.toString} XSL transformation ended")

    Future.successful(documentNode.toString().getBytes())
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
