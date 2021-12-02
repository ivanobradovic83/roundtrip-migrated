package service.authormapper.cache

import components.publishone.MetadataApi
import helpers.ScalaSpec
import helpers.TestUtils._
import org.mockito.Mockito.{reset, when}
import util.PublishOneConstants._
import util.{ConfigUtils, NodeTypes}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class ValueListCacheSpec extends ScalaSpec {

  lazy val cacheValueListTypes = Seq(
    documentTypeAuthor -> NodeTypes.Document,
    documentTypeAuthor -> NodeTypes.Folder
  )

  val configUtils: ConfigUtils = mock[ConfigUtils]
  val metadataApi: MetadataApi = mock[MetadataApi]
  val cut = new ValueListCache(configUtils, metadataApi)

  override def beforeEach(): Unit = {
    reset(configUtils)
    reset(metadataApi)
    cut.cleanCache()
  }

  "when cache initialized it should contain valid data" in {
    mockCache

    cut.valueListItemsCache(listItemsAuthor) should contain allOf ("Zondag, W.A." -> "8853", "Jong, E.S. de" -> "8854")
    cut.valueListItemsCache(listItemsPrefix) should contain allOf ("dr." -> "6025", "ing." -> "6027")
    cut.valueListItemsCache(listItemsFamilyNamePrefix) should contain allOf ("de" -> "6044", "van" -> "6038")
    cut.valueListItemsCache(listItemsGender) should contain allOf ("f" -> "6052", "m" -> "6051")
    cut.valueListItemsCache(listItemsPublicationName) should contain allOf ("pk99" -> "6559", "c-ara" -> "6442")
    cut.valueListItemsCache(listItemsRole) should contain allOf ("auteur" -> "6582", "muk" -> "6589")
    cut.valueListIdCache should contain allOf (listItemsAuthor -> 8, listItemsPrefix -> 53, listItemsFamilyNamePrefix -> 55, listItemsGender -> 56, listItemsPublicationName -> 58, listItemsRole -> 59)
  }

  "when cache initialized and cleaned it should be empty" in {
    mockCache

    cut.cleanCache()
    assertCacheEmpty
  }

  "when cache initialized return valid value list item id" in {
    mockCache

    cut.mapValueListItemId(listItemsAuthor, "Zondag, W.A.") should be("8853")
    cut.mapValueListItemId(listItemsPrefix, "ing.") should be("6027")
    cut.mapValueListItemId(listItemsFamilyNamePrefix, "de") should be("6044")
    cut.mapValueListItemId(listItemsGender, "m") should be("6051")
    cut.mapValueListItemId(listItemsPublicationName, "c-ara") should be("6442")
    cut.mapValueListItemId(listItemsRole, "auteur") should be("6582")
  }

  "when cache initialized return valid value list id" in {
    mockCache

    cut.mapValueListId(listItemsRole) should be(Some(59))
    cut.mapValueListId(listItemsAuthor) should be(Some(8))
    cut.mapValueListId(listItemsFamilyNamePrefix) should be(Some(55))
    cut.mapValueListId(listItemsPublicationName) should be(Some(58))
  }

  "when single list item added to cache then verify it exists" in {
    mockCache
    cut.mapValueListItemId(listItemsRole, "test") should be(empty)

    cut.addToCache(listItemsRole, "test", "123")
    cut.mapValueListItemId(listItemsRole, "test") should be("123")
  }

  private def mockCache = {
    assertCacheEmpty
    mockData
    assertDataMocked
  }

  private def assertDataMocked = {
    Await.result(cut.initCache(cacheValueListTypes), 10.seconds)
    cut.valueListItemsCache should have size 6
    cut.valueListIdCache should have size 6
  }

  private def assertCacheEmpty = {
    cut.valueListItemsCache should be(empty)
    cut.valueListIdCache should be(empty)
  }

  private def mockData = {
    when(configUtils.publishOneAuthorsRootFolderId).thenReturn(1)
    when(metadataApi.getMetadataDefinitions(documentTypeAuthor, NodeTypes.Document)).thenReturn(mockedJsonResp("metadata-author-document"))
    when(metadataApi.getMetadataDefinitions(documentTypeAuthor, NodeTypes.Folder)).thenReturn(mockedJsonResp("metadata-author-folder"))
    when(metadataApi.getValueListItems("/api/valuelists/8/items")).thenReturn(mockedJsonResp("value-list-8"))
    when(metadataApi.getValueListItems("/api/valuelists/53/items")).thenReturn(mockedJsonResp("value-list-53"))
    when(metadataApi.getValueListItems("/api/valuelists/55/items")).thenReturn(mockedJsonResp("value-list-55"))
    when(metadataApi.getValueListItems("/api/valuelists/56/items")).thenReturn(mockedJsonResp("value-list-56"))
    when(metadataApi.getValueListItems("/api/valuelists/58/items")).thenReturn(mockedJsonResp("value-list-58"))
    when(metadataApi.getValueListItems("/api/valuelists/59/items")).thenReturn(mockedJsonResp("value-list-59"))
  }

}
