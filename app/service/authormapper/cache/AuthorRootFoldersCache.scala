package service.authormapper.cache

import components.publishone.NodeApi
import play.api.libs.json.JsValue
import service.authormapper.model.AuthorFolder
import util.ConfigUtils
import util.PublishOneUtils._

import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/** This class caches PublishOne author root folders:
  *   - 1st level folders (e.g. A,B,C...)
  *   - 2nd level folders (e.g. Aa,Ab..,Ba,Bb...)
  *
  * @param configUtils
  *   configuration
  * @param nodeApi
  *   PublishOne Node API
  */
@Singleton
class AuthorRootFoldersCache @Inject()(configUtils: ConfigUtils, nodeApi: NodeApi) {

  lazy val rootFoldersCache: TrieMap[String, Int] = new TrieMap[String, Int]()

  def initCache: Future[Any] =
    if (configUtils.publishOneAuthorsRootFolderId != -1) cacheFirstLevelFolders.map(_ => cacheSecondLevelFolders)
    else Future.successful(())

  def cleanCache(): Unit = rootFoldersCache.clear()

  private def cacheFirstLevelFolders = cacheFolders(configUtils.publishOneAuthorsRootFolderId)

  private def cacheSecondLevelFolders = Future.sequence(rootFoldersCache.values.map(cacheFolders))

  private def cacheFolders(parentFolderId: Int) =
    getAllChildNodes(parentFolderId).map(_.map(folder => rootFoldersCache += (folder.title.toLowerCase -> folder.id)))

  private def getAllChildNodes(parentId: Int, pageNumber: Int = 1, pageSize: Int = 100): Future[Seq[AuthorFolder]] = {
    nodeApi
      .getChildNodes(parentId, pageNumber, pageSize)
      .flatMap(
        response => {
          val folders = responseToAuthorFolders((response \ "items").as[JsValue])
          val totalResults = (response \ "total").as[Int]
          if (totalResults > pageNumber * pageSize) getAllChildNodes(parentId, pageNumber + 1, pageSize).map(folders ++ _)
          else Future.successful(folders)
        }
      )
  }

}
