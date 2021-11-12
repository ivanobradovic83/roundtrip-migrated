package service

import components.sws.SwsApi
import dto.RoundTripDto
import play.api.Logger

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class RoundTripService @Inject()(swsClient: SwsApi,
                                 xmlTransformationService: XmlTransformationService,
                                 p1ImportService: P1ImportService,
                                 p1PublicationService: P1PublicationService) {

  private lazy val log = Logger(getClass)

  def roundTrip(roundTripDto: RoundTripDto): Future[Unit] = Future {
    log.info(s"${roundTripDto.toString} started")
    roundTripFlow(roundTripDto)
      .onComplete(result => onRoundTripComplete(result, roundTripDto))
  }

  private def roundTripFlow(roundTripDto: RoundTripDto) = {
    for {
      (xhtml, metaXml) <- swsClient.getXhtml(roundTripDto.docKey) zip swsClient.getMetaXml(roundTripDto.docKey)
      (transformedDocumentXml, transformedMetaXml) <- xmlTransformationService.transform(roundTripDto, xhtml, metaXml)
      importedDoc <- p1ImportService.importDocument(roundTripDto, transformedDocumentXml)
      status <- p1PublicationService.publish(roundTripDto, importedDoc)
    } yield status
  }

  private def onRoundTripComplete[A](result: Try[A], roundTripDto: RoundTripDto): Unit = result match {
    case Failure(exception) => log.error(s"${roundTripDto.toString} failed", exception)
    case Success(_)         => log.info(s"${roundTripDto.toString} done successfully")
  }

}
