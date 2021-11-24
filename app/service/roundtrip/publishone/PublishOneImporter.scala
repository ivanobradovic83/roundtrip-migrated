package service.roundtrip.publishone

import components.publishone.{DocumentApi, FolderApi, NodeOperationApi}
import dto.{ImportedDocumentDto, RoundTripDto}
import play.api.Logger
import util.PublishOneConstants._
import util.PublishOneUtils.docTypePath

import java.nio.charset.StandardCharsets
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
class PublishOneImporter @Inject()(folderApi: FolderApi, documentApi: DocumentApi, nodeOpsApi: NodeOperationApi) {

  private lazy val log = Logger(getClass)

  def importDocument(roundTripDto: RoundTripDto, content: Array[Byte], docMetadata: Map[String, String]): Future[ImportedDocumentDto] = {
    log.info(s"${roundTripDto.toString} Import document started")
    for {
      folderId <- createFolder(roundTripDto)
      docId <- createDocument(roundTripDto, folderId, docMetadata)
      _ <- setDocumentContent(roundTripDto, docId, content)
      _ <- Future.successful(log.info(s"${roundTripDto.toString} Document $docId imported"))
    } yield ImportedDocumentDto(folderId, roundTripDto.docKey, Seq(docId))
  }

  private def createFolder(roundTripDto: RoundTripDto): Future[Int] = {
    log.info(s"${roundTripDto.toString} Create folder started")
    folderApi
      .createFolder(roundTripDto.destination.toInt, roundTripDto.docKey, docTypePath(documentTypeCommenter))
      .map(response => {
        val id = (response \ "id").as[Int]
        log.info(s"${roundTripDto.toString} Folder $id created")
        id
      })
  }

  private def createDocument(roundTripDto: RoundTripDto, folderId: Int, docMetadata: Map[String, String]): Future[Int] = {
    log.info(s"${roundTripDto.toString} Create document in folder $folderId started")
    documentApi
      .createDocument(folderId, roundTripDto.docKey, docTypePath(documentTypeCommenter), docMetadata)
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
