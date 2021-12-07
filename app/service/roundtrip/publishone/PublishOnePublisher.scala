package service.roundtrip.publishone

import components.publishone.{NodeOperationApi, PublicationApi}
import play.api.Logger
import play.api.libs.ws.WSResponse
import service.roundtrip.model.{ImportedDocument, RoundTripDocument}
import util.ConfigUtils

import java.io.File
import javax.inject.Inject
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
class PublishOnePublisher @Inject()(configUtils: ConfigUtils, publicationApi: PublicationApi, nodeOpsApi: NodeOperationApi) {

  private lazy val log = Logger(getClass)
  private lazy val contentDispositionDocumentNamePattern = ".*filename=(.*?);.*".r

  def publish(roundTripDoc: RoundTripDocument, importedDoc: ImportedDocument): Future[Unit] = {
    lazy val logPrefix = s"$roundTripDoc $importedDoc"
    log.info(s"$logPrefix Publishing document")
    for {
      _ <- changeDocumentsStateFromCreatedToPublish(logPrefix, importedDoc.documentIds)
      (ticket, resultId) <- createPublication(logPrefix, importedDoc)
      response <- getFinishedPublicationFile(logPrefix, ticket, resultId)
      fileTransferStatus <- transferFileToFtp(logPrefix, response)
      _ <- deletePublicationIfFIleTransferred(logPrefix, fileTransferStatus, ticket)
    } yield log.debug(s"$logPrefix Document is published")
  }

  private def changeDocumentsStateFromCreatedToPublish(logPrefix: String, docIds: Seq[Int]): Future[Seq[Unit]] = {
    log.debug(s"$logPrefix Changing documents state from Aanmaken to Publiceren")
    Future.sequence(docIds.map(changeStateFromCreatedToPublish(logPrefix, _)))
  }

  private def changeStateFromCreatedToPublish(logPrefix: String, docId: Int): Future[Unit] = {
    log.debug(s"$logPrefix Changing document $docId state from Aanmaken to Publiceren")
    nodeOpsApi
      .changeState(docId, configUtils.documentStateCreated, configUtils.documentStatePublish)
      .map(_ => log.debug(s"$logPrefix Document $docId state changed from Aanmaken to Publiceren"))
  }

  private def createPublication(logPrefix: String, importedDoc: ImportedDocument): Future[(String, String)] = {
    log.debug(s"$logPrefix Creating document publication")
    publicationApi
      .createPublication(configUtils.publicationProfileOnlineZip, importedDoc.folderId, importedDoc.folderName)
      .andThen(_ => log.debug(s"$logPrefix Document publication is created"))
  }

  private def getFinishedPublicationFile(logPrefix: String, ticket: String, resultId: String) = {
    log.debug(s"$logPrefix Getting publication file")
    publicationApi
      .getFinishedPublicationFile(configUtils.publicationProfileOnlineZip, ticket, resultId)
      .andThen(_ => log.debug(s"$logPrefix Publication file retrieved"))
  }

  private def transferFileToFtp(logPrefix: String, res: WSResponse): Future[Boolean] = Future {
    // in this PoC implementation file is stored in local file system
    log.debug(s"$logPrefix Storing publication file to file system")
    val contentDispositionDocumentNamePattern(documentName) = res.header("Content-disposition").getOrElse("")
    val file = new File(s"./$documentName")
    val outputStream = java.nio.file.Files.newOutputStream(file.toPath)
    outputStream.write(res.bodyAsBytes.toArray)
    outputStream.close()
    log.debug(s"$logPrefix Publication file stored to file system ${file.getAbsolutePath}")
    true
  }

  private def deletePublicationIfFIleTransferred(logPrefix: String, fileTransferStatus: Boolean, ticket: String): Future[Unit] = {
    val deletePublicationLogPrefix = s"$logPrefix Deleting publication ${configUtils.publicationProfileOnlineZip}/$ticket"
    if (fileTransferStatus) deletePublication(deletePublicationLogPrefix, ticket)
    else deletePublicationWarning(deletePublicationLogPrefix)
  }

  private def deletePublication(deletePublicationLogPrefix: String, ticket: String) = {
    log.debug(s"$deletePublicationLogPrefix")
    publicationApi
      .deletePublication(configUtils.publicationProfileOnlineZip, ticket)
      .map(_ => log.debug(s"$deletePublicationLogPrefix done"))
  }

  private def deletePublicationWarning(deletePublicationLogPrefix: String) = {
    log.warn(s"$deletePublicationLogPrefix is not done because file transfer was not successful")
    Future.unit
  }

}
