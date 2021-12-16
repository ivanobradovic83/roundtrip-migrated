package components.publishone

import play.api.Logger
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import util.ConfigUtils
import util.PublishOneConstants.apiPublications

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/** PublishOne Publication API
  *
  * @param configUtils
  *   configuration
  * @param wsClient
  *   web client
  * @param accessTokenHandler
  *   access token handler
  */
class PublicationApi @Inject()(configUtils: ConfigUtils, wsClient: WSClient, accessTokenHandler: AccessTokenHandler)
    extends BasicApi(configUtils, wsClient, accessTokenHandler) {

  private lazy val log = Logger(getClass)

  def createPublication(profileId: String, nodeId: Int, folderName: String): Future[(String, String)] = {
    val requestBody = createPublicationRequestBody(nodeId, folderName)
    for {
      response <- postJson(s"$apiPublications/$profileId", requestBody)
      (ticket, resultId) <- checkPublicationCreatedState(profileId, response)
    } yield (ticket, resultId)
  }

  def getPublicationState(profileId: String, ticket: String): Future[JsValue] = {
    getJson(s"$apiPublications/$profileId/$ticket")
  }

  def getFinishedPublicationFile(profileId: String, ticket: String, resultId: String): Future[WSResponse] = {
    get(s"$apiPublications/$profileId/$ticket/$resultId")
  }

  def deletePublication(profileId: String, ticket: String): Future[Unit] = {
    delete(s"$apiPublications/$profileId/$ticket")
  }

  private def createPublicationRequestBody(nodeId: Int, folderName: String) = {
    val node = Json.obj(
      "nodeId" -> nodeId,
      "includeDescendants" -> true
    )
    Json.obj(
      "selectedNodes" -> Seq(node),
      "production" -> true,
      "baseFileName" -> folderName
    )
  }

  private def checkPublicationCreatedState(
      profileId: String,
      response: JsValue,
      counter: Int = configUtils.checkOperationStateMaxAttempts
  ): Future[(String, String)] = {
    log.debug(s"Checking publication file state: $response")
    val ticket = (response \ "path").as[String].split("/").last
    val state = (response \ "state").as[String]
    state match {
      case "succeeded" => publicationCreatedSuccessResponse(response, ticket)
      case "failed"    => throw new Exception(s"Publication creation failed: $response")
      case _           => recheckPublicationCreatedState(profileId, ticket, counter)
    }
  }

  private def publicationCreatedSuccessResponse(response: JsValue, ticket: String) = {
    val result = (response \ "results").as[Seq[JsObject]].head
    val resultId = (result \ "path").as[String].split("/").last
    Future.successful((ticket, resultId))
  }

  private def recheckPublicationCreatedState(profileId: String, ticket: String, counter: Int): Future[(String, String)] = {
    if (counter > 0) {
      waitAndRecheckPublicationCreatedState(profileId, ticket, counter)
    } else {
      throw new Exception(
        s"Publication creation $profileId/$ticket state checked maximum number " +
          s"of attempts ($configUtils.checkOperationStateMaxAttempts) without detecting success state"
      )
    }
  }

  private def waitAndRecheckPublicationCreatedState(profileId: String, ticket: String, counter: Int): Future[(String, String)] = {
    Thread.sleep(configUtils.checkOperationStateDelay)
    for {
      response <- getPublicationState(profileId, ticket)
      status <- checkPublicationCreatedState(profileId, response, counter - 1)
    } yield status
  }
}
