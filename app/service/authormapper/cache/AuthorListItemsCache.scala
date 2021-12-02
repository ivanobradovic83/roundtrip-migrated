package service.authormapper.cache

import components.publishone.MetadataApi
import play.api.Logger
import play.api.libs.json.JsValue
import util.PublishOneConstants.listItemsAuthor
import util.StringUtils.isAllDigits

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * This class caches author list item next id per first letter of family name.
  *
  * Key is created in the next format &lt;letter>&lt;counter> (e.g. L001) where:
  *   - &lt;letter> represent first letter (upper case) of author's family name
  *   - &lt;counter> represent next ordinal number for authors which family name starts with the &lt;letter>
  *
  * At the beginning:
  *   - all keys of author list items are transformed to (&lt;letter>,&lt;counter>) tuple
  *   - only valid counters are filtered (counters which contains only digits)
  *   - list of tuples is grouped by &lt;letter> and maximum counter value is calculated (per &lt;letter>)
  *   - &lt;letter> and calculated max &lt;counter> incremented by 1 (this value is used to generate new author item id)
  *     are stored in cache
  *
  * @param valueListCache PublishOne value lists cache
  */
@Singleton
class AuthorListItemsCache @Inject()(metadataApi: MetadataApi, valueListCache: ValueListCache) {

  private lazy val log = Logger(getClass)

  lazy val nextAuthorKeyNo: TrieMap[String, AtomicInteger] = new TrieMap[String, AtomicInteger]()

  def initCache(): Future[Unit] =
    metadataApi.getValueListItems(getAuthorValueListId).map(cacheNextAuthorItemCounters)

  def cleanCache(): Unit = nextAuthorKeyNo.clear()

  def getNextAuthorItemId(letter: String): String = {
    val nextCounter = getNextAuthorItemCounter(letter)
    f"$letter$nextCounter%03d"
  }

  def getNextAuthorItemCounter(letter: String): Int = nextAuthorKeyNo.getOrElseUpdate(letter, new AtomicInteger(1)).getAndIncrement()

  private def getAuthorValueListId: Int = {
    val valueListId = valueListCache.mapValueListId(listItemsAuthor)
    if (valueListId.isEmpty) {
      log.error(s"Missing cached $listItemsAuthor value list id")
      throw new Exception(s"Missing cached $listItemsAuthor value list id")
    }
    valueListId.get
  }

  private def cacheNextAuthorItemCounters(itemsResp: JsValue): Unit =
    calculateNextAuthorItemIdPerFirstLetterOfFamilyName(itemsResp)
      .foreach { case (letter, counter) => nextAuthorKeyNo += (letter -> new AtomicInteger(counter + 1)) }

  private def calculateNextAuthorItemIdPerFirstLetterOfFamilyName(itemsResp: JsValue): Map[String, Int] =
    itemKeys(itemsResp)
      .map(key => (key.take(1), key.drop(1)))
      .filter { case (_, counter) => isAllDigits(counter) }
      .map { case (letter, counter) => (letter, counter.toInt) }
      .groupMapReduce(_._1)(_._2)(_.max(_))

  private def itemKeys(itemsResp: JsValue): Seq[String] =
    itemsResp
      .as[Seq[JsValue]]
      .map(item => (item \ "key").asOpt[String].getOrElse(""))
      .filter(key => key.nonEmpty && key.length > 1)

}
