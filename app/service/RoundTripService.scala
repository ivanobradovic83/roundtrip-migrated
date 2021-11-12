package service

import components.publishone.FolderApi
import components.sws.SwsApi
import dto.{ImportedDocumentDto, RoundTripDto}
import play.api.Logger

import java.io.ByteArrayInputStream
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class RoundTripService @Inject()(swsClient: SwsApi,
                                 publishOneImportService: PublishOneImportService,
                                 xmlTransformationService: XmlTransformationService,
                                 publishOnePublishService: PublishOnePublishService) {

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
      importedDoc <- publishOneImportService.importDocument(roundTripDto, publishOneDocXml)
      status <- publishOnePublishService.publish(roundTripDto, importedDoc)
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

}
