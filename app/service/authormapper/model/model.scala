package service.authormapper.model

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Reads}
import util.CreationStatus
import util.CreationStatus.CreationStatus

case class AuthorFolder(folderStatus: CreationStatus, id: Int, title: String, authorItemStatus: CreationStatus, var authorItemId: String = null)

object AuthorFolder {
  lazy implicit val authorFolderReads: Reads[AuthorFolder] = (
    Reads.pure(CreationStatus.Existing) and (JsPath \ "id").read[Int] and (JsPath \ "title")
      .read[String] and Reads.pure(CreationStatus.Existing) and Reads.pure("")
  )(AuthorFolder.apply _)
}

case class AuthorDocument(status: CreationStatus, id: Int, title: String)

object AuthorDocument {
  lazy implicit val authorDocumentReads: Reads[AuthorDocument] = (
    Reads.pure(CreationStatus.Existing) and (JsPath \ "id").read[Int] and (JsPath \ "title").read[String]
  )(AuthorDocument.apply _)
}

case class Author(identifier: String,
                  name: String,
                  familyName: String,
                  givenName: String,
                  initials: String,
                  familyNamePrefix: String,
                  prefix: String,
                  gender: String,
                  publicationName: String,
                  documentIdentifier: String)

object Author {

  def apply(node: scala.xml.Node, publicationName: String, documentIdentifier: String): Author = {
    val identifier = (node \ "identifier").text
    val name = (node \ "name").text
    val familyName = (node \ "familyName").text
    val givenName = (node \ "givenName").text
    val initials = (node \ "initials").text
    val familyNamePrefix = (node \ "familyNamePrefix").text
    val prefix = (node \ "prefix").text
    val gender = (node \ "gender").text
    Author(identifier, name, familyName, givenName, initials, familyNamePrefix, prefix, gender, publicationName, documentIdentifier)
  }

}
