package service

import java.nio.charset.StandardCharsets
import util.PublishOneConstants._
import components.publishone.{DocumentApi, FolderApi, NodeOperationApi}
import dto.{ImportedDocumentDto, RoundTripDto}
import play.api.Logger

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * This class imports document to PublishOne by executing next steps:
  *  <pre>
  * - create PublishOne folder where imported document will be placed
  * - create PublishOne document with metadata
  * - set given XML content to the document
  * </pre>
  * @param folderApi PublishOne Folder API
  * @param documentApi PublishOne Document API
  * @param nodeOpsApi PublishOne NodeOperation API
  */
class PublishOneImportService @Inject()(folderApi: FolderApi, documentApi: DocumentApi, nodeOpsApi: NodeOperationApi) {

  private lazy val log = Logger(getClass)

  def importDocument(roundTripDto: RoundTripDto, content: Array[Byte]): Future[ImportedDocumentDto] = {
    log.info(s"${roundTripDto.toString} Import document started")
    for {
      folderId <- createFolder(roundTripDto)
      docId <- createDocument(roundTripDto, folderId)
      _ <- setDocumentContent(roundTripDto, docId, content)
      _ <- Future.successful(log.info(s"${roundTripDto.toString} Document $docId imported"))
    } yield ImportedDocumentDto(folderId, roundTripDto.docKey, Seq(docId))
  }

  private def createFolder(roundTripDto: RoundTripDto): Future[Int] = {
    log.info(s"${roundTripDto.toString} Create folder started")
    folderApi
      .createFolder(roundTripDto.destination.toInt, roundTripDto.docKey, documentTypeCommenter)
      .map(response => {
        val id = (response \ "id").as[Int]
        log.info(s"${roundTripDto.toString} Folder $id created")
        id
      })
  }

  private def createDocument(roundTripDto: RoundTripDto, folderId: Int): Future[Int] = {
    log.info(s"${roundTripDto.toString} Create document in folder $folderId started")
    documentApi
      .createDocument(folderId, roundTripDto.docKey, documentTypeCommenter)
      .map(response => {
        val docId = (response \ "id").as[Int]
        log.info(s"${roundTripDto.toString} Document $docId created in folder $folderId")
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
