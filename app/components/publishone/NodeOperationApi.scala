package components.publishone

import common.PublishOneConstants._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

/**
  * PublishOne NodeOperation API
  *
  * @param config configuration
  * @param wsClient web client
  * @param accessTokenHandler access token handler
  */
class NodeOperationApi @Inject()(config: Configuration, wsClient: WSClient, accessTokenHandler: AccessTokenHandler)
    extends BasicApi(config, wsClient, accessTokenHandler) {

  private lazy val log = Logger(getClass)

  private val checkNodeOpsSuccessStateMaxAttempts =
    Try("publishOne.nodeOps.checkSuccessResponse.maxAttempts").map(config.get[Int]).getOrElse(0)
  private val checkNodeOpsSuccessStateDelay =
    Try("publishOne.nodeOps.checkSuccessResponse.delay").map(config.get[Long]).getOrElse(0L)

  def getNodeOperationStatus(id: Int): Future[JsValue] = {
    getJson(s"$apiNodeOps/$id")
  }

  def assignCurrentUserAsAuthor(docId: Int): Future[Boolean] = {
    for {
      userId <- accessTokenHandler.userId
      requestBody <- assignAuthorRequestBody(docId, userId)
      response <- postJson(apiOpsAssign, requestBody)
      status <- checkNodeOpsSuccessState(response)
    } yield status
  }

  def changeState(docId: Int, currentStateId: Int, nextStateId: Int): Future[Boolean] = {
    val requestBody = stateChangeRequestBody(docId, currentStateId, nextStateId)
    for {
      response <- postJson(apiOpsStateChange, requestBody)
      status <- checkNodeOpsSuccessState(response)
    } yield status
  }

  private def checkNodeOpsSuccessState(response: JsValue, counter: Int = checkNodeOpsSuccessStateMaxAttempts): Future[Boolean] = {
    log.debug(s"Checking node operation status: $response")
    val id = (response \ "id").as[Int]
    val state = (response \ "state").as[String]
    state match {
      case "succeeded" => Future.successful(true)
      case "failed"    => throw new Exception(s"Node operation failed: $response")
      case _           => recheckNodeOpsSuccessState(id, counter)
    }
  }

  private def recheckNodeOpsSuccessState(id: Int, counter: Int): Future[Boolean] = {
    if (counter > 0) {
      Thread.sleep(checkNodeOpsSuccessStateDelay)
      for {
        response <- getNodeOperationStatus(id)
        status <- checkNodeOpsSuccessState(response, counter - 1)
      } yield status
    } else {
      log.warn(
        s"Node operation ($id) state checked maximum number of attempts " +
          s"($checkNodeOpsSuccessStateMaxAttempts) without detecting success state")
      Future.successful(false)
    }
  }

  private def assignAuthorRequestBody(docId: Int, userId: String) = {
    val selectedNode = Json.obj(
      "nodeId" -> docId,
      "includeDescendants" -> false
    )
    val authorAssignment = Json.obj(
      "roleId" -> authorRoleId,
      "userId" -> userId,
      "isGroup" -> false
    )
    Future.successful(
      Json.obj(
        "selectedNodes" -> Seq(selectedNode),
        "assignments" -> Seq(authorAssignment)
      ))
  }

  private def stateChangeRequestBody(docId: Int, currentStateId: Int, nextStateId: Int) = {
    val selectedNode = Json.obj(
      "nodeId" -> docId,
      "includeDescendants" -> true
    )
    Json.obj(
      "selectedNodes" -> Seq(selectedNode),
      "currentStateId" -> currentStateId,
      "nextStateId" -> nextStateId
    )
  }

}
