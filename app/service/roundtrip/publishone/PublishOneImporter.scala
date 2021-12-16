package service.roundtrip.publishone

import components.publishone.{DocumentApi, FolderApi, LinkApi, NodeOperationApi}
import play.api.Logger
import service.roundtrip.model.{AuthorDocumentMapping, ImportedDocument, RoundTripDocument}
import util.PublishOneConstants._
import util.StringUtils.notEmpty

import java.nio.charset.StandardCharsets
import javax.inject.Inject
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/** This class imports document to PublishOne by executing next steps: <pre>
  *   - create PublishOne folder where imported document will be placed
  *   - create PublishOne document with metadata
  *   - set given XML content to the document </pre>
  * @param folderApi
  *   PublishOne Folder API
  * @param documentApi
  *   PublishOne Document API
  * @param nodeOpsApi
  *   PublishOne NodeOperation API
  */
class PublishOneImporter @Inject()(folderApi: FolderApi, documentApi: DocumentApi, nodeOpsApi: NodeOperationApi, linkApi: LinkApi) {

  private lazy val log = Logger(getClass)

  def importDocument(
      roundTripDoc: RoundTripDocument,
      docContent: Array[Byte],
      folderMetadata: Map[String, AnyRef],
      docMetadata: Map[String, AnyRef]
  ): Future[ImportedDocument] = {
    log.info(s"$roundTripDoc Importing CWC document to PublishOne")
    for {
      folderId <- createFolder(roundTripDoc, folderMetadata)
      docId <- createDocument(roundTripDoc, folderId, docMetadata)
      _ <- setDocumentContent(roundTripDoc, docId, docContent)
      _ <- createAuthorLinks(roundTripDoc, folderId, docMetadata)
    } yield {
      log.debug(s"$roundTripDoc CWC document imported to PublishOne $docId")
      ImportedDocument(folderId, roundTripDoc.docKey, Seq(docId))
    }
  }

  private def createFolder(roundTripDoc: RoundTripDocument, folderMetadata: Map[String, AnyRef]): Future[Int] = {
    log.debug(s"$roundTripDoc Creating folder")
    val folderMetadataString = convertMetadataToStringValues(folderMetadata)
    folderApi
      .createFolder(roundTripDoc.destination.toInt, roundTripDoc.docKey, roundTripDoc.docType, folderMetadataString)
      .map { response =>
        val id = (response \ "id").as[Int]
        log.debug(s"$roundTripDoc Folder $id created")
        id
      }
  }

  private def createDocument(roundTripDoc: RoundTripDocument, folderId: Int, docMetadata: Map[String, AnyRef]): Future[Int] = {
    log.debug(s"$roundTripDoc Creating document in folder $folderId")
    val docMetadataString = convertMetadataToStringValues(docMetadata)
    documentApi
      .createDocument(folderId, roundTripDoc.docKey, roundTripDoc.docType, docMetadataString)
      .map { response =>
        val docId = (response \ "id").as[Int]
        log.debug(s"$roundTripDoc Document $docId created in folder $folderId")
        docId
      }
  }

  private def convertMetadataToStringValues(docMetadata: Map[String, AnyRef]): Map[String, String] = {
    val docMetadataString = new mutable.HashMap[String, String]()
    docMetadata.collect { case (key, value: String) => docMetadataString += (key -> value) }
    val authorListItemIds = getAuthorDocuments(docMetadata)
      .filter(authorDoc => notEmpty(authorDoc.authorItemId))
      .map(_.authorItemId)
      .mkString(",")
    if (notEmpty(authorListItemIds)) docMetadataString += (listItemsAuthor -> s"[$authorListItemIds]")
    docMetadataString.toMap
  }

  private def setDocumentContent(roundTripDoc: RoundTripDocument, docId: Int, content: Array[Byte]): Future[Unit] = {
    log.debug(s"$roundTripDoc Setting document content $docId")
    documentApi
      .uploadDocumentContent(docId, new String(content, StandardCharsets.UTF_8))
      .map(_ => log.debug(s"$roundTripDoc Document $docId content set"))
  }

  private def createAuthorLinks(roundTripDoc: RoundTripDocument, folderId: Int, docMetadata: Map[String, AnyRef]): Future[Any] = {
    log.debug(s"$roundTripDoc Creating links to author documents in folder $folderId")
    Future.sequence(getAuthorDocuments(docMetadata).map(createAuthorLinks(roundTripDoc, folderId, _)))
  }

  private def getAuthorDocuments(docMetadata: Map[String, AnyRef]): Seq[AuthorDocumentMapping] =
    docMetadata.get(listItemsAuthor).map(_.asInstanceOf[Seq[AuthorDocumentMapping]]).getOrElse(Seq.empty)

  private def createAuthorLinks(roundTripDoc: RoundTripDocument, folderId: Int, authorDoc: AuthorDocumentMapping): Future[Unit] = {
    val logPrefix: String = s"$roundTripDoc $authorDoc"
    log.debug(s"$logPrefix Creating link to author document in folder $folderId")
    linkApi
      .createInternalLink(folderId, authorDoc.title, roundTripDoc.docType, authorDoc.id)
      .map(_ => log.debug(s"$logPrefix Link to author document in folder $folderId created"))
  }

}
