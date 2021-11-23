package util

object PublishOneConstants {

  val authorRoleId = "0628f370-937c-45d7-a2fd-08dc88c325fd"
  val documentStateCreated = 176
  val documentStateWrite = 177
  val documentStatePublish = 182

  val documentTypeKeyAuthor = "auteursbeschrijvingen"
  val documentTypeKeyCommenter = "commentaar"
  val documentTypeApiPrefix = "/api/documenttypes"
  val documentTypeAuthor = s"$documentTypeApiPrefix/$documentTypeKeyAuthor"
  val documentTypeCommenter = s"$documentTypeApiPrefix/$documentTypeKeyCommenter"

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
  val apiDocumentTypes = s"$apiPrefix/documenttypes"

}

object NodeTypes extends Enumeration {
  type NodeType = Value
  val Document, Folder = Value
}
