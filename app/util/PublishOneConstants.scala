package util

object PublishOneConstants {

  val authorRoleId = "0628f370-937c-45d7-a2fd-08dc88c325fd"
  val documentStateCreated = 176
  val documentStateWrite = 177
  val documentStatePublish = 182

  val documentTypeCommenter = "/api/documenttypes/commentaar"
  val documentTypeCommenterId = "commentaar"
  val xsltLocation = "/xslt"
  val xsltRootLocation = "./conf/xslt"

  val apiPrefix = "api"
  val apiFolders = s"$apiPrefix/folders"
  val apiDocuments = s"$apiPrefix/documents"
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
