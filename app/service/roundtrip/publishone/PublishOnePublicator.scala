package service.roundtrip.publishone

import components.publishone.{NodeOperationApi, PublicationApi}
import dto.{ImportedDocumentDto, RoundTripDto}
import play.api.Logger
import play.api.libs.ws.WSResponse
import util.PublishOneConstants.{documentStateCreated, documentStatePublish, publicationProfileOnlineZip}

import java.io.File
import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  *  This class publishes document from PublishOne to CWC by executing next steps:
  *  <pre>
  * - set Publiceren state to all documents in imported folder
  * - create publication at PublishOne
  * - download publication file after it's created
  * - save publication to local file system (PoC implementation)
  *   in production ready implementation publication file should be streamed from PublishOne to FTP server
  * - delete publication from PublishOne
  * </pre>
  * @param publicationApi PublishOne Publication API
  * @param nodeOpsApi PublishOne NodeOperation API
  */
class PublishOnePublicator @Inject()(publicationApi: PublicationApi, nodeOpsApi: NodeOperationApi) {

  private lazy val log = Logger(getClass)
  private lazy val contentDispositionDocumentNamePattern = ".*filename=(.*?);.*".r

  def publish(roundTripDto: RoundTripDto, importedDocDto: ImportedDocumentDto): Future[Unit] = {
    lazy val logPrefix = s"$roundTripDto $importedDocDto"
    log.info(s"$logPrefix Publishing started")
    publishFlow(logPrefix, importedDocDto)
      .map(_ => log.info(s"$logPrefix Publishing done"))
  }

  private def publishFlow(logPrefix: String, importedDocDto: ImportedDocumentDto) = {
    for {
      _ <- changeDocumentsStateFromCreatedToPublish(logPrefix, importedDocDto.documentIds)
      (ticket, resultId) <- createPublication(logPrefix, importedDocDto)
      response <- getFinishedPublicationFile(logPrefix, ticket, resultId)
      fileTransferStatus <- transferFileToFtp(logPrefix, response)
      _ <- deletePublicationIfFIleTransferred(logPrefix, fileTransferStatus, ticket)
    } yield fileTransferStatus
  }

  private def changeDocumentsStateFromCreatedToPublish(logPrefix: String, docIds: Seq[Int]): Future[ListBuffer[Unit]] = {
    log.info(s"$logPrefix Changing documents state from Aanmaken to Publiceren started")
    val changeAllDocumentsStateInParallel = ListBuffer[Future[Unit]]()
    for (docId <- docIds) {
      changeAllDocumentsStateInParallel += changeStateFromCreatedToPublish(logPrefix, docId)
    }
    Future.sequence(changeAllDocumentsStateInParallel)
  }

  private def changeStateFromCreatedToPublish(logPrefix: String, docId: Int): Future[Unit] = {
    log.info(s"$logPrefix Changing document $docId state from Aanmaken to Publiceren started")
    nodeOpsApi
      .changeState(docId, documentStateCreated, documentStatePublish)
      .map(_ => log.info(s"$logPrefix Document $docId state changed from Aanmaken to Publiceren"))
  }

  private def createPublication(logPrefix: String, importedDocDto: ImportedDocumentDto) = {
    log.info(s"$logPrefix Creating publication started")
    for {
      (ticket, resultId) <- publicationApi.createPublication(publicationProfileOnlineZip, importedDocDto.folderId, importedDocDto.folderName)
      _ <- Future.successful(log.info(s"$logPrefix Publication created"))
    } yield (ticket, resultId)
  }

  private def getFinishedPublicationFile(logPrefix: String, ticket: String, resultId: String) = {
    log.info(s"$logPrefix Getting publication file started")
    for {
      response <- publicationApi.getFinishedPublicationFile(publicationProfileOnlineZip, ticket, resultId)
      _ <- Future.successful(log.info(s"$logPrefix Publication file retrieved"))
    } yield response
  }

  private def transferFileToFtp(logPrefix: String, res: WSResponse): Future[Boolean] = {
    // in this PoC implementation file is stored in local file system
    log.info(s"$logPrefix Storing publication file to file system")
    val contentDispositionDocumentNamePattern(documentName) = res.header("Content-disposition").getOrElse("")
    val file = new File(s"./$documentName")
    val outputStream = java.nio.file.Files.newOutputStream(file.toPath)
    outputStream.write(res.bodyAsBytes.toArray)
    outputStream.close()
    log.info(s"$logPrefix Publication file stored to file system ${file.getAbsolutePath}")
    Future.successful(true)
  }

  private def deletePublicationIfFIleTransferred(logPrefix: String, fileTransferStatus: Boolean, ticket: String): Future[Unit] = {
    val deletePublicationLogPrefix = s"$logPrefix Delete publication $publicationProfileOnlineZip/$ticket"
    if (fileTransferStatus) deletePublication(deletePublicationLogPrefix, ticket)
    else deletePublicationWarning(deletePublicationLogPrefix)
  }

  private def deletePublication(deletePublicationLogPrefix: String, ticket: String) = {
    log.info(s"$deletePublicationLogPrefix started")
    publicationApi
      .deletePublication(publicationProfileOnlineZip, ticket)
      .map(_ => log.info(s"$deletePublicationLogPrefix done"))
  }

  private def deletePublicationWarning(deletePublicationLogPrefix: String) = {
    log.warn(s"$deletePublicationLogPrefix is not done because file transfer was not successful")
    Future.unit
  }

}
