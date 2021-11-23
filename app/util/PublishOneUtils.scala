package util

import play.api.libs.json.{JsValue, Json, Reads}
import service.authormapper.model.AuthorFolder
import PublishOneConstants._

object PublishOneUtils {

  private lazy implicit val authorFolderReads: Reads[AuthorFolder] = Json.reads[AuthorFolder]

  def responseToAuthorFolders(resp: JsValue): Seq[AuthorFolder] =
    resp.as[Seq[JsValue]].filter(isAuthorFolder).map(Json.fromJson[AuthorFolder](_).get)

  def isAuthorFolder(node: JsValue): Boolean =
    (node \ "nodeType").as[String] == "folder" && (node \ "documentTypePath").as[String] == documentTypeAuthor

}
