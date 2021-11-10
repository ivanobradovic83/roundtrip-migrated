package util

object PublishOneConstants {

  val authorRoleId = "0628f370-937c-45d7-a2fd-08dc88c325fd"
  val documentStateCreated = 176
  val documentStateWrite = 177
  val documentStatePublish = 182

  val documentTypeCommenter = "/api/documenttypes/commentaar"

  lazy val apiPrefix = "api"
  lazy val apiFolders = s"$apiPrefix/folders"
  lazy val apiDocuments = s"$apiPrefix/documents"
  lazy val apiNodeOps = s"$apiPrefix/nodeoperations"
  lazy val apiOps = s"$apiPrefix/operations"
  lazy val apiOpsAssign = s"$apiOps/assign"
  lazy val apiOpsStateChange = s"$apiOps/statechange"
  lazy val apiPublications = s"$apiPrefix/publications"

}
