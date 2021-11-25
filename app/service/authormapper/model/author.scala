package service.authormapper.model

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Reads}

case class AuthorFolder(id: Int, title: String, var authorItemId: String = null)

object AuthorFolder {
  lazy implicit val authorFolderReads: Reads[AuthorFolder] = (
    (JsPath \ "id").read[Int] and (JsPath \ "title").read[String] and Reads.pure("")
    )(AuthorFolder.apply _)
}

case class AuthorDocument(id: Int, title: String)

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
