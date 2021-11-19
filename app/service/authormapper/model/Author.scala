package service.authormapper.model

case class Author(identifier: String, name: String, familyName: String, givenName: String, initials: String, documentIdentifier: String)

object Author {
  def apply(node: scala.xml.Node, documentIdentifier: String): Author = {
    val identifier = (node \ "identifier").text
    val name = (node \ "name").text
    val familyName = (node \ "familyName").text
    val givenName = (node \ "givenName").text
    val initials = (node \ "initials").text
    Author(identifier, name, familyName, givenName, initials, documentIdentifier)
  }

}
