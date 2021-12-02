package service.authormapper.cache

import components.publishone.MetadataApi
import helpers.ScalaSpec
import helpers.TestUtils.mockedJsonResp
import org.mockito.Mockito.{reset, when}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import util.PublishOneConstants.listItemsAuthor

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class AuthorListItemsCacheSpec extends ScalaSpec {

  lazy val authorsValueListId = 1

  val metadataApi: MetadataApi = mock[MetadataApi]
  val valueListCache: ValueListCache = mock[ValueListCache]
  val cut = new AuthorListItemsCache(metadataApi, valueListCache)

  override def beforeEach(): Unit = {
    reset(valueListCache)
    cut.cleanCache()
  }

  "initialized cache for empty value list should be empty" in {
    when(valueListCache.mapValueListId(listItemsAuthor)).thenReturn(Some(authorsValueListId))
    when(metadataApi.getValueListItems(authorsValueListId)).thenReturn(mockedJsonResp("value-list-cache-empty-8"))

    Await.result(cut.initCache(), 10.seconds)
    cut.nextAuthorKeyNo should be(empty)
  }

  "initialized cache for existing value list should contain valid data" in {
    when(valueListCache.mapValueListId(listItemsAuthor)).thenReturn(Some(authorsValueListId))
    when(metadataApi.getValueListItems(authorsValueListId)).thenReturn(mockedJsonResp("value-list-cache-8"))

    Await.result(cut.initCache(), 10.seconds)
    cut.nextAuthorKeyNo should have size 2
    cut.nextAuthorKeyNo("L").get should be(51)
    cut.nextAuthorKeyNo("T").get should be(9)

  }

  "when empty cache, generates item id for counter 1" in {
    val letter = "L"
    cut.getNextAuthorItemId(letter) should equal("L001")
    cut.nextAuthorKeyNo must have size 1
    cut.nextAuthorKeyNo should contain key letter
    cut.nextAuthorKeyNo(letter).get should be(2)
    cut.cleanCache()
    cut.nextAuthorKeyNo should be(empty)
  }

  "when author list item id missing in cache then throw exception" in {
    when(valueListCache.mapValueListId(listItemsAuthor)).thenReturn(None)

    val exception = the[Exception] thrownBy cut.initCache()
    exception.getMessage should be(s"Missing cached $listItemsAuthor value list id")
  }

}
