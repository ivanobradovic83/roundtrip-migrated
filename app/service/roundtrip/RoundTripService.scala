package service.roundtrip

import components.sws.SwsApi
import dto.RoundTripDto
import play.api.Logger
import service.roundtrip.metadata.MetadataMapper
import service.roundtrip.publishone.{PublishOneImporter, PublishOnePublicator}
import service.roundtrip.xml.SwsToPublishOneXmlTransformer

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class RoundTripService @Inject()(swsClient: SwsApi,
                                 swsToPublishOneXmlTransformer: SwsToPublishOneXmlTransformer,
                                 metadataMapper: MetadataMapper,
                                 publishOneImporter: PublishOneImporter,
                                 publishOnePublicator: PublishOnePublicator) {

  private lazy val log = Logger(getClass)

  def roundTrip(roundTripDto: RoundTripDto): Future[Unit] = Future {
    log.info(s"${roundTripDto.toString} started")
    roundTripFlow(roundTripDto)
      .onComplete(result => onRoundTripComplete(result, roundTripDto))
  }

  private def roundTripFlow(roundTripDto: RoundTripDto) = {
    for {
      (xhtml, metaXml) <- swsClient.getXhtml(roundTripDto.docKey) zip swsClient.getMetaXml(roundTripDto.docKey)
      (publishOneDocumentXml, publishOneMetaXml) <- swsToPublishOneXmlTransformer.transform(roundTripDto, xhtml, metaXml)
      docJsonMeta <- metadataMapper.mapXmlToJsonMetadata(publishOneMetaXml)
      importedDoc <- publishOneImporter.importDocument(roundTripDto, publishOneDocumentXml, docJsonMeta)
      status <- publishOnePublicator.publish(roundTripDto, importedDoc)
    } yield status
  }

  private def onRoundTripComplete[A](result: Try[A], roundTripDto: RoundTripDto): Unit = result match {
    case Failure(exception) => log.error(s"${roundTripDto.toString} failed", exception)
    case Success(_)         => log.info(s"${roundTripDto.toString} done successfully")
  }

}