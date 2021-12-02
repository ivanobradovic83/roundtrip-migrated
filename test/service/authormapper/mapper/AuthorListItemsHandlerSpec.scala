package service.authormapper.mapper

import components.publishone.MetadataApi
import helpers.ScalaSpec
import helpers.TestUtils.mockedJsonResp
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import service.authormapper.cache.{AuthorListItemsCache, ValueListCache}
import util.PublishOneConstants.listItemsAuthor

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class AuthorListItemsHandlerSpec extends ScalaSpec {

  lazy val itemName: String = "Jong, E.S. de"
  lazy val itemId: String = "8854"
  lazy val itemKey: String = "J051"
  lazy val authorValueListId: Int = 45
  lazy val expectedXmlContent: String =
    s"""<?xml version="1.0" encoding="utf-8"?>
    |<valuelist>
    |\t<item>
    |\t\t<key>$itemKey</key>
    |\t\t<value>$itemName</value>
    |\t</item>
    |</valuelist>""".stripMargin

  val metadataApi: MetadataApi = mock[MetadataApi]
  val valueListCache: ValueListCache = mock[ValueListCache]
  val authorListItemsCache: AuthorListItemsCache = mock[AuthorListItemsCache]
  val cut = new AuthorListItemsHandler(metadataApi, valueListCache, authorListItemsCache)

  override def beforeEach(): Unit = {
    reset(metadataApi)
    reset(valueListCache)
    reset(authorListItemsCache)
  }

  "when author list item id missing in cache then throw exception" in {
    when(valueListCache.mapValueListId(listItemsAuthor)).thenReturn(None)

    val exception = the[Exception] thrownBy cut.findOrCreate(itemName)
    exception.getMessage should be(s"Missing cached $listItemsAuthor value list id")
  }

  "when author list item exist for given name then return it's id" in {
    when(valueListCache.mapValueListItemId(listItemsAuthor, itemName)).thenReturn(itemId)

    val result = Await.result(cut.findOrCreate(itemName), 10.seconds)
    result should be(itemId)
    verifyNoInteractions(metadataApi)
  }

  "when author list item not exist for given name then create new and return it's id" in {
    when(valueListCache.mapValueListItemId(listItemsAuthor, itemName)).thenReturn("")
    when(valueListCache.mapValueListId(listItemsAuthor)).thenReturn(Some(authorValueListId))
    when(metadataApi.getValueListItems(authorValueListId)).thenReturn(mockedJsonResp("value-list-8"))
    when(authorListItemsCache.getNextAuthorItemId("J")).thenReturn(itemKey)
    when(metadataApi.createValueListItem(ArgumentMatchers.eq(authorValueListId), any())).thenReturn(Future.successful())

    val result = Await.result(cut.findOrCreate(itemName), 10.seconds)
    result should be(itemId)
    verify(metadataApi).createValueListItem(authorValueListId, expectedXmlContent)
    verify(valueListCache).addToCache(listItemsAuthor, itemName, itemId)
  }

}
