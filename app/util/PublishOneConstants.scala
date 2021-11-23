package util

object PublishOneConstants {

  val authorRoleId = "0628f370-937c-45d7-a2fd-08dc88c325fd"
  val documentStateCreated = 176
  val documentStateWrite = 177
  val documentStatePublish = 182

  val documentTypeAuthor = "/api/documenttypes/auteursbeschrijvingen"
  val documentTypeKeyAuthor = "auteursbeschrijvingen"
  val documentTypeCommenter = "/api/documenttypes/commentaar"
  val documentTypeKeyCommenter = "commentaar"

  val listItemsFamilyNamePrefixId = 55
  val listItemsPrefixId = 53
  val listItemsGenderId = 56
  val listItemsRoleId = 65
  val listItemsPublicationNameId = 67
  val listItemsPublicationId = 28

  val listItemsFamilyNamePrefix = "familyNamePrefix"
  val listItemsPrefix = "prefix"
  val listItemsGender = "gender"
  val listItemsRole = "role"
  val listItemsPublicationName = "publicationName"
  val listItemsPublication = "publication"

  val xsltLocation = "/xslt"
  val xsltRootLocation = "./conf/xslt"

  val apiPrefix = "api"
  val apiFolders = s"$apiPrefix/folders"
  val apiDocuments = s"$apiPrefix/documents"
  val apiNodes = s"$apiPrefix/nodes"
  val apiNodeOps = s"$apiPrefix/nodeoperations"
  val apiOps = s"$apiPrefix/operations"
  val apiOpsAssign = s"$apiOps/assign"
  val apiOpsStateChange = s"$apiOps/statechange"
  val apiPublications = s"$apiPrefix/publications"

  val methodPost = "POST"
  val methodPut = "PUT"
  val methodGet = "GET"
  val methodDelete = "DELETE"

}

object NodeTypes extends Enumeration {
  type NodeType = Value
  val document, folder = Value
}
