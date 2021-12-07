package util

import play.api.libs.json.{JsValue, Json, Reads}
import PublishOneConstants._
import service.authormapper.model.{AuthorDocument, AuthorFolder}

object PublishOneUtils {

  private lazy implicit val authorDocumentReads: Reads[AuthorDocument] = Json.reads[AuthorDocument]

  def responseToAuthorFolders(resp: JsValue): Seq[AuthorFolder] =
    resp.as[Seq[JsValue]].filter(isAuthorFolder).map(Json.fromJson[AuthorFolder](_).get)

  def isAuthorFolder(node: JsValue): Boolean =
    (node \ "nodeType").as[String] == "folder" && (node \ "documentTypePath").as[String] == docTypePath(documentTypeAuthor)

  def responseToAuthorDocuments(resp: JsValue): Seq[AuthorDocument] =
    resp.as[Seq[JsValue]].filter(isAuthorDocument).map(Json.fromJson[AuthorDocument](_).get)

  def isAuthorDocument(node: JsValue): Boolean =
    (node \ "nodeType").as[String] == "document" && (node \ "documentTypePath").as[String] == docTypePath(documentTypeAuthor)

  def docTypePath(docTypeKey: String) = s"$documentTypePathPrefix/$docTypeKey"

  def getMetadataFields(resp: JsValue): Seq[JsValue] = (resp \\ "metadataFields").toSeq.flatMap(_.as[Seq[JsValue]])

}
