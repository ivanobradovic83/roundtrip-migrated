package service

import java.nio.charset.StandardCharsets

import util.PublishOneConstants._
import components.publishone.{DocumentApi, NodeOperationApi}
import dto.RoundTripDto
import play.api.Logger
import javax.inject.Inject

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * This class imports document to PublishOne by executing next steps:
  *  <pre>
  * - create PublishOne document with metadata
  * - set given XML content to the document
  * </pre>
  * @param documentApi PublishOne Document API
  * @param nodeOpsApi PublishOne NodeOperation API
  */
class PublishOneImportService @Inject()(documentApi: DocumentApi, nodeOpsApi: NodeOperationApi) {

  private lazy val log = Logger(getClass)

  def importDocument(roundTripDto: RoundTripDto, content: Array[Byte]): Future[Int] = {
    log.info(s"${roundTripDto.toString} Import document started")
    for {
      docId <- createDocument(roundTripDto)
      _ <- setDocumentContent(roundTripDto, docId, content)
      _ <- Future.successful(log.info(s"${roundTripDto.toString} Document $docId imported"))
    } yield docId
  }

  private def createDocument(roundTripDto: RoundTripDto): Future[Int] = {
    log.info(s"${roundTripDto.toString} Create document started")
    documentApi
      .createDocument(roundTripDto.destination.toInt, roundTripDto.docKey, documentTypeCommenter)
      .map(response => {
        val docId = (response \ "id").as[Int]
        val docName = (response \ "title").as[String]
        log.info(s"${roundTripDto.toString} Document $docId with name $docName created")
        docId
      })
  }

  private def setDocumentContent(roundTripDto: RoundTripDto, docId: Int, content: Array[Byte]) = {
    log.info(s"${roundTripDto.toString} Setting document content $docId started")
    for {
      _ <- documentApi.uploadDocumentContent(docId, new String(content, StandardCharsets.UTF_8))
      result <- Future.successful(log.info(s"${roundTripDto.toString} Document $docId content set"))
    } yield result
  }

}
