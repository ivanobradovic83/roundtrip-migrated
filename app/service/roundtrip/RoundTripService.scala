package service.roundtrip

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorAttributes.withSupervisionStrategy
import akka.stream.ClosedShape
import akka.stream.Supervision.resumingDecider
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink}
import components.publishone.AccessTokenHandler
import components.sws.{SwsApi, SwsSourceApi}
import dto.RoundTripDto
import play.api.Logger
import service.common.cache.ValueListCache
import service.common.logging.LoggingService
import service.common.monithoring.InProgressHandler
import service.roundtrip.metadata.{AuthorDocumentMapper, MetadataMapper}
import service.roundtrip.model.{ImportedDocument, RoundTripDocument}
import service.roundtrip.publishone.{PublishOneImporter, PublishOnePublisher}
import service.roundtrip.xslt.SwsToPublishOneXmlTransformer
import util.{ConfigUtils, NodeTypes, RoundTripActions}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * This service creates and runs Akka streams flow which round-trip selected documents by
  * importing them to PublishOne and republishing back to CWC
  *
  * @param swsClient CWC SWS Client
  * @param swsSourceApi CWC SWS Akka source
  * @param swsToPublishOneXmlTransformer SWS to PublishOne xml transformer
  * @param metadataMapper PublishOne metadata mapper
  * @param publishOneImporter PublishOne document importer
  * @param publishOnePublisher PublishOne document publisher
  */
class RoundTripService @Inject()(
    configUtils: ConfigUtils,
    loggingService: LoggingService,
    inProgressHandler: InProgressHandler,
    accessTokenHandler: AccessTokenHandler,
    valueListCache: ValueListCache,
    swsClient: SwsApi,
    swsSourceApi: SwsSourceApi,
    swsToPublishOneXmlTransformer: SwsToPublishOneXmlTransformer,
    authorDocumentMapper: AuthorDocumentMapper,
    metadataMapper: MetadataMapper,
    publishOneImporter: PublishOneImporter,
    publishOnePublisher: PublishOnePublisher
) {

  private lazy val log = Logger(getClass)
  private implicit val system: ActorSystem = ActorSystem("RoundTripSystem")
  type RTDocContents = (RoundTripDocument, Array[Byte], Array[Byte])
  type P1FolderDocMeta = (Map[String, AnyRef], Map[String, AnyRef])
  type P1ContentFolderDocMeta = (RoundTripDocument, Array[Byte], Map[String, AnyRef], Map[String, AnyRef])

  def roundTrip(roundTripDto: RoundTripDto): Future[Done] = {
    log.info(s"$roundTripDto started")

    val start = System.currentTimeMillis()
    initRoundTrip(roundTripDto.docType)
      .flatMap(_ => runRoundTripFlow(roundTripDto))
      .andThen(onRoundTripComplete(_, roundTripDto, start))
  }

  private def initRoundTrip(docType: String): Future[Unit] = {
    val cacheTypeMetadata = Seq(docType -> NodeTypes.Document, docType -> NodeTypes.Folder)
    inProgressHandler.startProcess(getClass.getSimpleName)
    for {
      _ <- accessTokenHandler.accessToken zip authorDocumentMapper.initCache
      _ <- valueListCache.initCache(cacheTypeMetadata) zip metadataMapper.initCache(cacheTypeMetadata)
    } yield ()
  }

  private def runRoundTripFlow(roundTripDto: RoundTripDto): Future[Done] = {
    val source = swsSourceApi.searchAndStreamDocs(roundTripDto.swsQuery)
    val toRoundTripDocFlow: Flow[String, RoundTripDocument, _] =
      Flow[String].map(docKey => RoundTripDocument(roundTripDto.id, docKey, roundTripDto.docType, roundTripDto.destination))

    val runnableGraph = RunnableGraph.fromGraph(
      GraphDSL.create(Sink.ignore) { implicit builder => sink =>
        import GraphDSL.Implicits._

        val toRoundTripDocFlowShape = builder.add(toRoundTripDocFlow)
        val fetchDocumentsFlowShape = builder.add(fetchDocumentsFlow)
        val transformSwsToPublishOneDataFlowShape = builder.add(transformSwsToPublishOneDataFlow)
        val importDocumentToPublishOneFlowFlowShape = builder.add(importDocumentToPublishOneFlow)

        source ~> toRoundTripDocFlowShape ~> fetchDocumentsFlowShape ~> transformSwsToPublishOneDataFlowShape ~> importDocumentToPublishOneFlowFlowShape
        if (roundTripDto.action == RoundTripActions.RoundTrip) importDocumentToPublishOneFlowFlowShape ~> builder.add(republishDocumentFlow) ~> sink
        else importDocumentToPublishOneFlowFlowShape ~> sink

        ClosedShape
      }
    )
    runnableGraph.withAttributes(withSupervisionStrategy(resumingDecider)).run()
  }

  private lazy val fetchDocumentsFlow: Flow[RoundTripDocument, RTDocContents, _] =
    Flow[RoundTripDocument].mapAsyncUnordered(configUtils.parallelism) { roundTripDoc =>
      (swsClient.getXhtml(roundTripDoc.docKey) zip swsClient.getMetaXml(roundTripDoc.docKey))
        .map { case (xhtml, metaXml) => (roundTripDoc, xhtml, metaXml) }
        .recover(recover(roundTripDoc, "fetchDocumentsFlow"))
    }

  private lazy val transformSwsToPublishOneDataFlow: Flow[RTDocContents, P1ContentFolderDocMeta, _] =
    Flow[RTDocContents].mapAsyncUnordered(configUtils.parallelism) {
      case (roundTripDoc, xhtml, metaXml) =>
        (for {
          (publishOneDocumentXml, publishOneMetaXml) <- swsToPublishOneXmlTransformer.transform(roundTripDoc, xhtml, metaXml)
          (folderMetadata, docMetadata) <- mapXmlMetadata(roundTripDoc, publishOneMetaXml)
        } yield (roundTripDoc, publishOneDocumentXml, folderMetadata, docMetadata))
          .recover(recover(roundTripDoc, "transformSwsToPublishOneDataFlow"))
    }

  private def mapXmlMetadata(roundTripDoc: RoundTripDocument, metadataXml: Array[Byte]): Future[P1FolderDocMeta] = {
    val folderJsonMetadata = metadataMapper.mapXmlMetadata(roundTripDoc, metadataXml, NodeTypes.Folder)
    val documentJsonMetadata = metadataMapper.mapXmlMetadata(roundTripDoc, metadataXml, NodeTypes.Document)
    folderJsonMetadata zip documentJsonMetadata
  }

  private lazy val importDocumentToPublishOneFlow: Flow[P1ContentFolderDocMeta, (RoundTripDocument, ImportedDocument), _] =
    Flow[P1ContentFolderDocMeta].mapAsyncUnordered(configUtils.parallelism) {
      case (roundTripDoc, publishOneDocumentXml, folderMetadata, docMetadata) =>
        publishOneImporter
          .importDocument(roundTripDoc, publishOneDocumentXml, folderMetadata, docMetadata)
          .map({
            loggingService.logEvent("P1_IMPORT", 0, roundTripDoc.docKey, success = true, "Document successfully imported to PublishOne")
            (roundTripDoc, _)
          })
          .recover(recover(roundTripDoc, "importDocumentToPublishOneFlow"))
    }

  private lazy val republishDocumentFlow: Flow[(RoundTripDocument, ImportedDocument), Unit, _] =
    Flow[(RoundTripDocument, ImportedDocument)].mapAsyncUnordered(configUtils.parallelism) {
      case (roundTripDoc, importedDocument) =>
        publishOnePublisher
          .publish(roundTripDoc, importedDocument)
          .map(res => {
            loggingService.logEvent("ROUNDTRIP", 0, roundTripDoc.docKey, success = true, "Document successfully published")
            res
          })
          .recover(recover(roundTripDoc, "republishDocumentFlow"))
    }

  private def recover[T](roundTripDoc: RoundTripDocument, flowInfo: String): PartialFunction[Throwable, T] = {
    case e: Throwable =>
      log.error(s"$roundTripDoc $flowInfo: ${e.getMessage}")
      flowInfo match {
        case "importDocumentToPublishOneFlow" =>
          loggingService.logEvent("P1_IMPORT", 0, roundTripDoc.docKey, success = false, s"Document import failed: ${e.getMessage}")
        case _ =>
          loggingService.logEvent("ROUNDTRIP", 0, roundTripDoc.docKey, success = false, s"Republishing document failed: ${e.getMessage}")
      }
      throw e
  }

  private def onRoundTripComplete[A](result: Try[A], roundTripDto: RoundTripDto, start: Long): Unit = {
    closeRoundTripProcess()
    val duration = (System.currentTimeMillis() - start) / 1000
    result match {
      case Failure(exception) => log.error(s"$roundTripDto failed", exception)
      case Success(_)         => log.info(s"$roundTripDto done successfully in $duration s")
    }
  }

  private def closeRoundTripProcess(): Unit = {
    inProgressHandler.stopProcess(getClass.getSimpleName)
    authorDocumentMapper.cleanCache()
    valueListCache.cleanCache()
    metadataMapper.cleanCache()
  }
}
