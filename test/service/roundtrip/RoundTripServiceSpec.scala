package service.roundtrip

import akka.stream.scaladsl.Source
import components.publishone.AccessTokenHandler
import components.sws.{SwsApi, SwsSourceApi}
import dto.RoundTripDto
import helpers.ScalaSpec
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mockito.verification.VerificationMode
import service.common.cache.ValueListCache
import service.common.logging.LoggingService
import service.common.monithoring.InProgressHandler
import service.roundtrip.metadata.{AuthorDocumentMapper, MetadataMapper}
import service.roundtrip.model.{ImportedDocument, RoundTripDocument}
import service.roundtrip.publishone.{PublishOneImporter, PublishOnePublisher}
import service.roundtrip.xslt.SwsToPublishOneXmlTransformer
import util.{ConfigUtils, NodeTypes, RoundTripActions}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class RoundTripServiceSpec extends ScalaSpec {

  lazy val processName = "RoundTripService"
  lazy val roundTripId = "round_trip_id"
  lazy val swsQuery = "sws_query"
  lazy val docType = "doc_type"
  lazy val destination = "destination"
  lazy val docKey1 = "docKey1"
  lazy val docKey2 = "docKey2"
  lazy val cacheTypeMetadata = Seq(docType -> NodeTypes.Document, docType -> NodeTypes.Folder)
  lazy val xhtmlContent: Array[Byte] = Array(1)
  lazy val xmlMetaContent: Array[Byte] = Array(2)
  lazy val publishOneDocumentXml: Array[Byte] = Array(3)
  lazy val publishOneMetaXml: Array[Byte] = Array(4)
  lazy val folderJsonMetadata: Map[String, AnyRef] = Map("key1" -> "val1")
  lazy val documentJsonMetadata: Map[String, AnyRef] = Map("key2" -> "val2")
  lazy val roundTripDoc1: RoundTripDocument = RoundTripDocument(roundTripId, docKey1, docType, destination)
  lazy val importedDoc1: ImportedDocument = ImportedDocument(123, "Folder title 1", Seq(456))
  lazy val roundTripDoc2: RoundTripDocument = RoundTripDocument(roundTripId, docKey2, docType, destination)
  lazy val importedDoc2: ImportedDocument = ImportedDocument(321, "Folder title 2", Seq(789))

  val configUtils: ConfigUtils = mock[ConfigUtils]
  val loggingService: LoggingService = mock[LoggingService]
  val inProgressHandler: InProgressHandler = mock[InProgressHandler]
  val accessTokenHandler: AccessTokenHandler = mock[AccessTokenHandler]
  val valueListCache: ValueListCache = mock[ValueListCache]
  val swsClient: SwsApi = mock[SwsApi]
  val swsSourceApi: SwsSourceApi = mock[SwsSourceApi]
  val swsToPublishOneXmlTransformer: SwsToPublishOneXmlTransformer = mock[SwsToPublishOneXmlTransformer]
  val authorDocumentMapper: AuthorDocumentMapper = mock[AuthorDocumentMapper]
  val metadataMapper: MetadataMapper = mock[MetadataMapper]
  val publishOneImporter: PublishOneImporter = mock[PublishOneImporter]
  val publishOnePublisher: PublishOnePublisher = mock[PublishOnePublisher]

  val cut = new RoundTripService(
    configUtils,
    loggingService,
    inProgressHandler,
    accessTokenHandler,
    valueListCache,
    swsClient,
    swsSourceApi,
    swsToPublishOneXmlTransformer,
    authorDocumentMapper,
    metadataMapper,
    publishOneImporter,
    publishOnePublisher
  )

  override def beforeEach(): Unit = {
    reset(configUtils)
    reset(inProgressHandler)
    reset(accessTokenHandler)
    reset(valueListCache)
    reset(swsClient)
    reset(swsSourceApi)
    reset(swsToPublishOneXmlTransformer)
    reset(authorDocumentMapper)
    reset(metadataMapper)
    reset(publishOneImporter)
    reset(publishOnePublisher)
  }

  "when round-trip started then verify all components are invoked" in {
    mockData()
    val roundTripDto: RoundTripDto = RoundTripDto(roundTripId, swsQuery, docType, destination, RoundTripActions.RoundTrip)

    Await.result(cut.roundTrip(roundTripDto), 10.seconds)

    verifyComponentCalls()
    verify(publishOnePublisher).publish(roundTripDoc1, importedDoc1)
    verify(publishOnePublisher).publish(roundTripDoc2, importedDoc2)
  }

  "when import only started then verify all components are invoked" in {
    mockData()
    val roundTripDto: RoundTripDto = RoundTripDto(roundTripId, swsQuery, docType, destination, RoundTripActions.ImportOnly)

    Await.result(cut.roundTrip(roundTripDto), 10.seconds)

    verifyComponentCalls()
    verifyNoInteractions(publishOnePublisher)
  }

  "when an exception occurs in flow then skip the error element and proceed" in {
    mockData()
    when(swsClient.getXhtml(docKey1)).thenReturn(Future.failed(new RuntimeException("Unexpected test exception")))
    val roundTripDto: RoundTripDto = RoundTripDto(roundTripId, swsQuery, docType, destination, RoundTripActions.RoundTrip)

    Await.result(cut.roundTrip(roundTripDto), 10.seconds)

    verifyComponentCalls(withError = true)
    verify(publishOnePublisher, never()).publish(roundTripDoc1, importedDoc1)
    verify(publishOnePublisher).publish(roundTripDoc2, importedDoc2)
  }

  private def mockData(): Unit = {
    when(configUtils.parallelism).thenReturn(1)
    when(accessTokenHandler.accessToken).thenReturn(Future.successful("at"))
    when(authorDocumentMapper.initCache).thenReturn(Future.unit)
    when(valueListCache.initCache(cacheTypeMetadata)).thenReturn(Future.unit)
    when(metadataMapper.initCache(cacheTypeMetadata)).thenReturn(Future.unit)
    when(swsSourceApi.searchAndStreamDocs(swsQuery)).thenReturn(Source(Seq(docKey1, docKey2)))
    when(swsClient.getXhtml(any())).thenReturn(Future.successful(xhtmlContent))
    when(swsClient.getMetaXml(any())).thenReturn(Future.successful(xmlMetaContent))
    when(swsToPublishOneXmlTransformer.transform(any(), ArgumentMatchers.eq(xhtmlContent), ArgumentMatchers.eq(xmlMetaContent)))
      .thenReturn(Future.successful((publishOneDocumentXml, publishOneMetaXml)))
    when(metadataMapper.mapXmlMetadata(any(), ArgumentMatchers.eq(publishOneMetaXml), ArgumentMatchers.eq(NodeTypes.Folder)))
      .thenReturn(Future.successful(folderJsonMetadata))
    when(metadataMapper.mapXmlMetadata(any(), ArgumentMatchers.eq(publishOneMetaXml), ArgumentMatchers.eq(NodeTypes.Document)))
      .thenReturn(Future.successful(documentJsonMetadata))
    when(publishOneImporter.importDocument(roundTripDoc1, publishOneDocumentXml, folderJsonMetadata, documentJsonMetadata))
      .thenReturn(Future.successful(importedDoc1))
    when(publishOneImporter.importDocument(roundTripDoc2, publishOneDocumentXml, folderJsonMetadata, documentJsonMetadata))
      .thenReturn(Future.successful(importedDoc2))
    when(publishOnePublisher.publish(any(), any())).thenReturn(Future.unit)
  }

  private def verifyComponentCalls(withError: Boolean = false): Unit = {
    verifyBeforeFlow()
    verifyDoc1(withError)
    verifyDoc2()
    verifyAfterFlow()
  }

  private def verifyBeforeFlow(): Unit = {
    verify(inProgressHandler).startProcess(processName)
    verify(accessTokenHandler).accessToken
    verify(authorDocumentMapper).initCache
    verify(valueListCache).initCache(cacheTypeMetadata)
    verify(metadataMapper).initCache(cacheTypeMetadata)
    verify(swsSourceApi).searchAndStreamDocs(swsQuery)
  }

  private def verifyAfterFlow(): Unit = {
    verify(inProgressHandler).stopProcess(processName)
    verify(authorDocumentMapper).cleanCache()
    verify(valueListCache).cleanCache()
    verify(metadataMapper).cleanCache()
  }

  private def verifyDoc1(withError: Boolean): Unit = {
    val noOfInvocations: VerificationMode = if (withError) never() else times(1)
    verify(swsClient).getXhtml(docKey1)
    verify(swsClient).getMetaXml(docKey1)
    verify(swsToPublishOneXmlTransformer, noOfInvocations).transform(roundTripDoc1, xhtmlContent, xmlMetaContent)
    verify(metadataMapper, noOfInvocations).mapXmlMetadata(roundTripDoc1, publishOneMetaXml, NodeTypes.Folder)
    verify(metadataMapper, noOfInvocations).mapXmlMetadata(roundTripDoc1, publishOneMetaXml, NodeTypes.Document)
    verify(publishOneImporter, noOfInvocations).importDocument(roundTripDoc1, publishOneDocumentXml, folderJsonMetadata, documentJsonMetadata)
  }

  private def verifyDoc2(): Unit = {
    verify(swsClient).getXhtml(docKey2)
    verify(swsClient).getMetaXml(docKey2)
    verify(swsToPublishOneXmlTransformer).transform(roundTripDoc2, xhtmlContent, xmlMetaContent)
    verify(metadataMapper).mapXmlMetadata(roundTripDoc2, publishOneMetaXml, NodeTypes.Folder)
    verify(metadataMapper).mapXmlMetadata(roundTripDoc2, publishOneMetaXml, NodeTypes.Document)
    verify(publishOneImporter).importDocument(roundTripDoc2, publishOneDocumentXml, folderJsonMetadata, documentJsonMetadata)
  }
}
