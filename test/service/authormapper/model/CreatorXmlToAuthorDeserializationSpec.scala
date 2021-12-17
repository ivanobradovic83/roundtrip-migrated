package service.authormapper.model

import helpers.ScalaSpec

import scala.xml.Elem

class CreatorXmlToAuthorDeserializationSpec extends ScalaSpec {

  lazy val publicationName: String = "c-ar"
  lazy val documentIdentifier: String = "c-AR-W3936-610"

  lazy val creatorXmlAllData: Elem =
    <creator>
      <identifier>J133</identifier>
      <role>auteur</role>
      <name>E.S. de Jong</name>
      <familyNamePrefix>de</familyNamePrefix>
      <familyName>Jong</familyName>
      <givenName>Eva</givenName>
      <prefix>mr.</prefix>
      <initials>E.S.</initials>
      <gender>f</gender>
    </creator>

  lazy val creatorXmlMinData: Elem =
    <creator>
      <identifier>J133</identifier>
      <familyName>Jong</familyName>
    </creator>

  lazy val authorAllData: Author = Author("J133", "E.S. de Jong", "Jong", "Eva", "E.S.", "de", "mr.", "f", publicationName, documentIdentifier)
  lazy val authorMinData: Author = Author("J133", "", "Jong", "", "", "", "", "", publicationName, documentIdentifier)

  "test creator mapping to author with all data" in {
    val result = Author.apply(creatorXmlAllData, publicationName, documentIdentifier)
    result should be(authorAllData)
  }

  "test creator mapping to author with minimal data" in {
    val result = Author.apply(creatorXmlMinData, publicationName, documentIdentifier)
    result should be(authorMinData)
  }

}
