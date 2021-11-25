package service.authormapper.cache

import components.publishone.{MetadataApi, NodeApi}
import play.api.libs.json.JsValue
import service.authormapper.model.AuthorFolder
import util.NodeTypes.NodeType
import util.PublishOneConstants._
import util.PublishOneUtils._
import util.{ConfigUtils, NodeTypes}

import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * This class caches commonly used PublishOne data
  *
  * @param configUtils configuration
  * @param nodeApi PublishOne Node API
  * @param metadataApi PublishOne Metadata API
  */
@Singleton
class PublishOneCache @Inject()(configUtils: ConfigUtils, nodeApi: NodeApi, metadataApi: MetadataApi) {

  private lazy val listItemsToCache =
    Seq(listItemsFamilyNamePrefix, listItemsPrefix, listItemsGender, listItemsRole, listItemsPublicationName, listItemsPublication)

  lazy val valueListItemsCache: TrieMap[String, TrieMap[String, String]] = new TrieMap[String, TrieMap[String, String]]()
  lazy val rootFoldersCache: TrieMap[String, Int] = new TrieMap[String, Int]()

  def initCache: Future[Any] = {
    Future
      .sequence(
        Seq(
          cacheRootFolders,
          cacheValueListMetadata(documentTypeAuthor, NodeTypes.Document),
          cacheValueListMetadata(documentTypeAuthor, NodeTypes.Folder)
        ))
  }

  def cleanCache(): Unit = {
    rootFoldersCache.clear()
    valueListItemsCache.clear()
  }

  def mapListItemValue(valueListName: String, key: String): String = valueListItemsCache(valueListName).getOrElse(key, "")

  private def cacheValueListMetadata(documentTypeKey: String, nodeType: NodeType) = {
    metadataApi
      .getMetadataDefinitions(documentTypeKey, nodeType)
      .map { resp =>
        getMetadataFields(resp).filter(shouldValueListMetadataFieldBeCached).map(cacheValueListMetadataField)
      }
  }

  private def shouldValueListMetadataFieldBeCached(metadataFieldDefinition: JsValue) =
    listItemsToCache.contains((metadataFieldDefinition \ "name").as[String])

  private def cacheValueListMetadataField(metadataFieldDefinition: JsValue) = {
    val name = (metadataFieldDefinition \ "name").as[String]
    val valueListPath = (metadataFieldDefinition \ "settings" \ "valueListPath").as[String]
    cacheValueListItems(name, valueListPath)
  }

  private def cacheValueListItems(valueListName: String, valueListPath: String) = {
    var cache = valueListItemsCache.get(valueListName)
    if (cache.isEmpty) {
      valueListItemsCache += (valueListName -> new TrieMap[String, String]())
      cache = valueListItemsCache.get(valueListName)
    }
    metadataApi
      .getValueListItems(valueListPath)
      .map(response =>
        response.as[Seq[JsValue]].map { item =>
          val key = valueListName match {
            case value if mapByName(value)         => (item \ "name").as[String]
            case value if value == listItemsGender => (item \ "key").as[String].substring(0, 1)
            case _                                 => (item \ "key").as[String]
          }
          val id = (item \ "id").as[Int].toString
          cache.get += (key -> id)
      })
  }

  private def mapByName(valueListName: String) = valueListName == listItemsFamilyNamePrefix || valueListName == listItemsPrefix

  private def cacheRootFolders =
    if (configUtils.publishOneAuthorsRootFolderId != -1) cacheFirstLevelFolders.map(_ => cacheSecondLevelFolders)
    else Future.successful()

  private def cacheFirstLevelFolders = cacheFolders(configUtils.publishOneAuthorsRootFolderId)

  private def cacheSecondLevelFolders = Future.sequence(rootFoldersCache.values.map(cacheFolders))

  private def cacheFolders(parentFolderId: Int) =
    getAllChildNodes(parentFolderId).map(_.map(folder => rootFoldersCache += (folder.title.toLowerCase -> folder.id)))

  private def getAllChildNodes(parentId: Int, pageNumber: Int = 1, pageSize: Int = 100): Future[Seq[AuthorFolder]] = {
    nodeApi
      .getChildNodes(parentId, pageNumber, pageSize)
      .flatMap(response => {
        val folders = responseToAuthorFolders((response \ "items").as[JsValue])
        val totalResults = (response \ "total").as[Int]
        if (totalResults > pageNumber * pageSize) getAllChildNodes(parentId, pageNumber + 1, pageSize).map(folders ++ _)
        else Future.successful(folders)
      })
  }

}
