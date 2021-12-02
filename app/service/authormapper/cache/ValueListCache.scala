package service.authormapper.cache

import components.publishone.MetadataApi
import play.api.libs.json.JsValue
import util.ConfigUtils
import util.NodeTypes.NodeType
import util.PublishOneConstants._
import util.PublishOneUtils._
import util.StringUtils.notEmpty

import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * This class caches PublishOne list items metadata for given document and node types.
  *
  * @param configUtils configuration
  * @param metadataApi PublishOne Metadata API
  */
@Singleton
class ValueListCache @Inject()(configUtils: ConfigUtils, metadataApi: MetadataApi) {

  lazy val valueListItemsCache: TrieMap[String, TrieMap[String, String]] = new TrieMap[String, TrieMap[String, String]]()
  lazy val valueListIdCache: TrieMap[String, Int] = new TrieMap[String, Int]()

  def initCache(types: Seq[(String, NodeType)]): Future[Any] =
    Future.sequence(types.map {
      case (documentType, nodeType) => cacheValueListMetadata(documentType, nodeType)
    })

  def cleanCache(): Unit = {
    valueListItemsCache.clear()
    valueListIdCache.clear()
  }

  def mapValueListItemId(valueListName: String, key: String): String = valueListItemsCache(valueListName).getOrElse(key, "")

  def mapValueListId(valueListName: String): Option[Int] = valueListIdCache.get(valueListName)

  def addToCache(valueListName: String, key: String, id: String): Unit = valueListItemsCache(valueListName) += (key -> id)

  private def cacheValueListMetadata(documentTypeKey: String, nodeType: NodeType): Future[Unit] =
    for {
      metadataFields <- metadataApi.getMetadataDefinitions(documentTypeKey, nodeType).map(getMetadataFields)
      _ <- Future.sequence(metadataFields.filter(isValueListMetadata).map(cacheValueListMetadata)).map(_ => ())
    } yield ()

  private def isValueListMetadata(metadataFieldDefinition: JsValue): Boolean =
    (metadataFieldDefinition \ "baseMetadataType").asOpt[String].contains("selectList")

  private def cacheValueListMetadata(metadataFieldDefinition: JsValue): Future[Unit] = {
    val nameOpt = (metadataFieldDefinition \ "name").asOpt[String]
    val valueListPathOpt = (metadataFieldDefinition \ "settings" \ "valueListPath").asOpt[String]
    (nameOpt, valueListPathOpt) match {
      case (Some(name), Some(valueListPath)) => loadValueListItems(name, valueListPath)
      case _                                 => Future.successful(())
    }
  }

  private def loadValueListItems(valueListName: String, valueListPath: String): Future[Unit] = {
    val valueListId = valueListPath.split("/")(3).toInt
    valueListIdCache += (valueListName -> valueListId)
    metadataApi.getValueListItems(valueListPath).map(cacheValueListItems(valueListName, _))
  }

  private def cacheValueListItems(valueListName: String, itemsResp: JsValue) =
    itemsResp.as[Seq[JsValue]].map { item =>
      val key: String = getListItemKey(valueListName, item)
      val id = (item \ "id").asOpt[Int].map(_.toString).orNull
      if (notEmpty(key) && notEmpty(id)) getValueListCache(valueListName) += (key -> id)
    }

  private def getListItemKey(valueListName: String, item: JsValue): String =
    valueListName match {
      case value if mapByName(value)         => (item \ "name").asOpt[String].orNull
      case value if value == listItemsGender => (item \ "key").asOpt[String].map(_.substring(0, 1)).orNull
      case _                                 => (item \ "key").asOpt[String].orNull
    }

  private def getValueListCache(valueListName: String): TrieMap[String, String] =
    valueListItemsCache.getOrElse(valueListName, {
      val cache = new TrieMap[String, String]()
      valueListItemsCache += (valueListName -> cache)
      cache
    })

  private def mapByName(valueListName: String): Boolean =
    valueListName == listItemsFamilyNamePrefix || valueListName == listItemsPrefix || valueListName == listItemsAuthor

}
