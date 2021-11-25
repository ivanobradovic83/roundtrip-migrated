package service.authormapper.model

case class AuthorFolder(id: Int, title: String)

case class AuthorDocument(id: Int, title: String, listItemId: String = null)

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
