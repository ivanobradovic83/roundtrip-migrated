package service.authormapper.mapper

import components.publishone.MetadataApi
import play.api.Logger
import play.api.libs.json.JsValue
import service.authormapper.cache.AuthorListItemsCache
import service.common.cache.ValueListCache
import util.PublishOneConstants.listItemsAuthor
import util.StringUtils.notEmpty

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/** This class handles author list items.
  *
  * It will first try to find it by item name. If it does not exist it will create new one.
  *
  * @param metadataApi
  *   PublishOne Metadata API
  * @param valueListCache
  *   PublishOne value list cache
  * @param authorListItemsCache
  *   PublishOne author list item cache
  */
class AuthorListItemsHandler @Inject()(metadataApi: MetadataApi, valueListCache: ValueListCache, authorListItemsCache: AuthorListItemsCache) {

  private lazy val log = Logger(getClass)

  def findOrCreate(itemName: String): Future[String] = {
    val itemId = valueListCache.mapValueListItemId(listItemsAuthor, itemName)
    if (notEmpty(itemId)) Future.successful(itemId)
    else create(itemName)
  }

  private def create(itemName: String): Future[String] = {
    val valueListId = getAuthorValueListId
    val itemKey = buildItemKey(itemName)
    val xmlContent = listItemToXml(itemKey, itemName)
    for {
      _ <- metadataApi.createValueListItem(valueListId, xmlContent)
      itemId <- metadataApi.getValueListItems(valueListId).map(findItemIdBy(_, itemKey).get)
      _ <- Future { valueListCache.addToCache(listItemsAuthor, itemName, itemId) }
    } yield itemId
  }

  private def getAuthorValueListId: Int = {
    val valueListId = valueListCache.mapValueListId(listItemsAuthor)
    if (valueListId.isEmpty) {
      log.error(s"Missing cached $listItemsAuthor value list id")
      throw new Exception(s"Missing cached $listItemsAuthor value list id")
    }
    valueListId.get
  }

  private def buildItemKey(name: String): String = {
    val letter = name.substring(0, 1).toUpperCase
    authorListItemsCache.getNextAuthorItemId(letter)
  }

  private def listItemToXml(key: String, name: String): String =
    s"""<?xml version="1.0" encoding="utf-8"?>
      |<valuelist>
      |\t<item>
      |\t\t<key>$key</key>
      |\t\t<value>$name</value>
      |\t</item>
      |</valuelist>""".stripMargin

  private def findItemIdBy(resp: JsValue, itemKey: String): Option[String] =
    resp
      .as[Seq[JsValue]]
      .map(itemToTuple)
      .filter { case (findBy, id) => findBy == itemKey && notEmpty(id) }
      .map(_._2)
      .headOption

  private def itemToTuple(item: JsValue): (String, String) = {
    val firstTupleValue: String = (item \ "key").asOpt[String].getOrElse("")
    val id: String = (item \ "id").asOpt[Int].map(_.toString).getOrElse("")
    (firstTupleValue, id)
  }

}
