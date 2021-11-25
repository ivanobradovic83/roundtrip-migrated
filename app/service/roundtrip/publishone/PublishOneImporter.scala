package service.roundtrip.publishone

import components.publishone.{DocumentApi, FolderApi, LinkApi, NodeOperationApi}
import dto.{ImportedDocumentDto, RoundTripDto}
import play.api.Logger
import service.authormapper.model.AuthorDocument
import util.PublishOneConstants._
import util.StringUtils.notEmpty

import java.nio.charset.StandardCharsets
import javax.inject.Inject
import scala.collection.mutable
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
class PublishOneImporter @Inject()(folderApi: FolderApi, documentApi: DocumentApi, nodeOpsApi: NodeOperationApi, linkApi: LinkApi) {

  private lazy val log = Logger(getClass)

  def importDocument(roundTripDto: RoundTripDto, content: Array[Byte], docMetadata: Map[String, AnyRef]): Future[ImportedDocumentDto] = {
    log.info(s"${roundTripDto.toString} Import document started")
    for {
      folderId <- createFolder(roundTripDto)
      docId <- createDocument(roundTripDto, folderId, docMetadata)
      _ <- setDocumentContent(roundTripDto, docId, content)
      _ <- createAuthorLinks(roundTripDto, folderId, docMetadata)
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

  private def createDocument(roundTripDto: RoundTripDto, folderId: Int, docMetadata: Map[String, AnyRef]): Future[Int] = {
    log.info(s"${roundTripDto.toString} Create document in folder $folderId started")
    val docMetadataString = convertMetadataToStringValues(docMetadata)
    documentApi
      .createDocument(folderId, roundTripDto.docKey, documentTypeCommenter, docMetadataString)
      .map(response => {
        val docId = (response \ "id").as[Int]
        log.info(s"${roundTripDto.toString} Document $docId created in folder $folderId")
        docId
      })
  }

  private def convertMetadataToStringValues(docMetadata: Map[String, AnyRef]) = {
    val docMetadataString = new mutable.HashMap[String, String]()
    docMetadata.collect { case (key, value: String) => docMetadataString += (key -> value) }
    val authorListItemIds = getAuthorDocuments(docMetadata)
      .filter(authorDoc => notEmpty(authorDoc.listItemId))
      .map(_.listItemId)
      .mkString(",")
    if (notEmpty(authorListItemIds)) docMetadataString += (listItemsAuthor -> s"[$authorListItemIds]")
    docMetadataString.toMap
  }

  private def setDocumentContent(roundTripDto: RoundTripDto, docId: Int, content: Array[Byte]) = {
    log.info(s"${roundTripDto.toString} Setting document content $docId started")
    for {
      _ <- documentApi.uploadDocumentContent(docId, new String(content, StandardCharsets.UTF_8))
      result <- Future.successful(log.info(s"${roundTripDto.toString} Document $docId content set"))
    } yield result
  }

  private def createAuthorLinks(roundTripDto: RoundTripDto, folderId: Int, docMetadata: Map[String, AnyRef]): Future[Any] = {
    log.info(s"${roundTripDto.toString} Creating links to author documents in folder $folderId started")
    Future.sequence(getAuthorDocuments(docMetadata).map(createAuthorLinks(roundTripDto, folderId, _)))
  }

  private def getAuthorDocuments(docMetadata: Map[String, AnyRef]) = {
    docMetadata
      .get(listItemsAuthor)
      .map { case value: Seq[AuthorDocument] => value }
      .getOrElse(Seq.empty)
  }

  private def createAuthorLinks(roundTripDto: RoundTripDto, folderId: Int, authorDoc: AuthorDocument) = {
    val logPrefix: String = s"${roundTripDto.toString} ${authorDoc.toString}"
    log.info(s"$logPrefix Creating link to author document in folder $folderId started")
    for {
      _ <- linkApi.createInternalLink(folderId, authorDoc.title, documentTypeCommenter, authorDoc.id)
      result <- Future.successful(log.info(s"$logPrefix Link to author document in folder $folderId created"))
    } yield result

  }

}
