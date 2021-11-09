package service

import common.PublishOneConstants._
import components.publishone.{DocumentApi, NodeOperationApi}
import dto.RoundTripDto
import play.api.Logger

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * This class imports document to PublishOne by executing next steps:
  * - creating PublishOne document
  * - assigning current system user as document Author
  * - changing document state from Created to Write
  * - setting given XML content to the document
  *
  * @param documentApi PublishOne Document API
  * @param nodeOpsApi PublishOne NodeOperation API
  */
class PublishOneImportService @Inject()(documentApi: DocumentApi, nodeOpsApi: NodeOperationApi) {

  private lazy val log = Logger(getClass)

  def importDocument(roundTripDto: RoundTripDto, content: Array[Byte]): Future[Int] = {
    log.info(s"${roundTripDto.toString} import PublishOne document started")
    for {
      docId <- createDocument(roundTripDto)
      _ <- assignAuthor(roundTripDto, docId) zip changeStateFromCreatedToWrite(roundTripDto, docId)
      _ <- setDocumentContent(roundTripDto, docId, content)
    } yield docId
  }

  private def createDocument(roundTripDto: RoundTripDto): Future[Int] = {
    log.info(s"${roundTripDto.toString} create PublishOne document started")
    documentApi
      .createDocument(roundTripDto.destination.toInt, s"${roundTripDto.docKey}-tmp", "/api/documenttypes/commentaar")
      .map(response => {
        val docId = (response \ "id").as[Int]
        val docName = (response \ "title").as[String]
        log.info(s"Created document: $docId with name: $docName")
        docId
      })
  }

  private def assignAuthor(roundTripDto: RoundTripDto, docId: Int) = {
    log.info(s"${roundTripDto.toString} assign PublishOne author started")
    nodeOpsApi
      .assignCurrentUserAsAuthor(docId)
      .map(_ => log.info(s"Author assigned to document: $docId"))
  }

  private def changeStateFromCreatedToWrite(roundTripDto: RoundTripDto, docId: Int) = {
    log.info(s"${roundTripDto.toString} change PublishOne document state from created to write started")
    nodeOpsApi
      .changeState(docId, documentStateCreated, documentStateWrite)
      .map(_ => log.info(s"State changed for document: $docId to write"))
  }

  private def setDocumentContent(roundTripDto: RoundTripDto, docId: Int, content: Array[Byte]) = {
    log.info(s"${roundTripDto.toString} set PublishOne document content started")
    documentApi
      .uploadDocumentContent(docId, "<document>bllaa</document>")
      .map(_ => log.info(s"Document $docId content set"))
  }

}
