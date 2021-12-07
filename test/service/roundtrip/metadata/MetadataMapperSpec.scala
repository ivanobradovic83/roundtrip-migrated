package service.roundtrip.metadata

import components.publishone.MetadataApi
import helpers.ScalaSpec
import helpers.TestUtils.mockedJsonResp
import org.mockito.Mockito.{reset, when}
import service.common.cache.ValueListCache
import service.roundtrip.model.{AuthorDocumentMapping, RoundTripDocument}
import util.NodeTypes
import util.PublishOneConstants._

import java.nio.file.{Files, Paths}
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class MetadataMapperSpec extends ScalaSpec {

  lazy val cacheValueListTypes = Seq(
    documentTypeAuthor -> NodeTypes.Document,
    documentTypeAuthor -> NodeTypes.Folder
  )
  lazy val cacheAuthorDocKey = s"$documentTypeAuthor-${NodeTypes.Document}"
  lazy val cacheAuthorFolderKey = s"$documentTypeAuthor-${NodeTypes.Folder}"
  lazy val roundTripDoc: RoundTripDocument = RoundTripDocument("round_trip_id", "koc_key", documentTypeAuthor, "dest")
  lazy val metadataXmlContent: Array[Byte] = Files.readAllBytes(Paths.get("test/resources/transformed-meta.xml"))

  val metadataApi: MetadataApi = mock[MetadataApi]
  val metadataAuthorMapper: AuthorDocumentMapper = mock[AuthorDocumentMapper]
  val valueListCache: ValueListCache = mock[ValueListCache]
  val cut: MetadataMapper = new MetadataMapper(metadataApi, metadataAuthorMapper, valueListCache)

  override def beforeEach(): Unit = {
    reset(metadataApi)
    reset(metadataAuthorMapper)
    reset(valueListCache)
    cut.cleanCache()
  }

  "when cache initialized it should contain valid data" in {
    mockCache()

    cut
      .metadataDefCache(cacheAuthorDocKey)
      .keySet should contain only (listItemsPublicationName, listItemsAuthor, "editorialDate", "contributor", "add-legalArea", "dc-keyword", "language", "documentKey", "rechtsthema")
    cut.metadataDefCache(cacheAuthorFolderKey).keySet should contain only ("legalArea", "dc-type", "isPartOf", "audience")
  }

  "when cache initialized and cleaned it should be empty" in {
    mockCache()

    cut.cleanCache()
    cut.metadataDefCache should be(empty)
  }

  "when cache initialized then map Document xml metadata to json" in {
    mockDocumentMapping()

    val result: Map[String, AnyRef] = Await.result(cut.mapXmlMetadata(roundTripDoc, metadataXmlContent, NodeTypes.Document), 10.seconds)
    result should have size 8
    result should contain("documentKey" -> "c-AAN-W43570-2.62")
    result should contain("dc-creator" -> List(AuthorDocumentMapping(123, "Author 1", "listItem123")))
    result should contain("editorialDate" -> "2020-09-17T00:00:00")
    result should contain("language" -> "list_item_lang_nl")
    result should contain("dc-keyword" -> """["word_1","word_2"]""")
    result should contain("add-legalArea" -> """[list_item_add_legal_1,list_item_add_legal_2]""")
    result should contain("rechtsthema" -> "[list_item_rechtsthema]")
    result should contain("publicationName" -> "[list_item_publ_name]")
  }

  "when empty cache then map Document xml metadata to json returns empty map" in {
    cut.metadataDefCache should be(empty)

    val result: Map[String, AnyRef] = Await.result(cut.mapXmlMetadata(roundTripDoc, metadataXmlContent, NodeTypes.Document), 10.seconds)
    result should be(empty)
  }

  "when cache initialized then map Folder xml metadata to json" in {
    mockFolderMapping()

    val result: Map[String, AnyRef] = Await.result(cut.mapXmlMetadata(roundTripDoc, metadataXmlContent, NodeTypes.Folder), 10.seconds)
    result should have size 4
    result should contain("legalArea" -> "[list_item_add_legal_1,list_item_add_legal_2]")
    result should contain("dc-type" -> "[list_item_type]")
    result should contain("isPartOf" -> "list_item_isPartOf")
    result should contain("audience" -> "[list_item_audience]")
  }

  "when missing metadata type then throw exception" in {
    cut.metadataDefCache should be(empty)
    when(metadataApi.getMetadataDefinitions(documentTypeAuthor, NodeTypes.Folder)).thenReturn(mockedJsonResp("metadata-comment-folder-invalid"))
    Await.result(cut.initCache(Seq(documentTypeAuthor -> NodeTypes.Folder)), 10.seconds)
    cut.metadataDefCache should have size 1
    cut.metadataDefCache(cacheAuthorFolderKey) should have size 1

    val exception = the[Exception] thrownBy Await.result(cut.mapXmlMetadata(roundTripDoc, metadataXmlContent, NodeTypes.Folder), 10.seconds)
    exception.getMessage should startWith("Missing baseMetadataType in {")
  }

  private def mockDocumentMapping(): Unit = {
    mockCache()
    when(metadataAuthorMapper.mapAuthorToDocument("tdankert")).thenReturn(Some(AuthorDocumentMapping(123, "Author 1", "listItem123")))
    when(metadataAuthorMapper.mapAuthorToDocument("slautenbag")).thenReturn(None)
    when(valueListCache.mapValueListItemId("language", "nl")).thenReturn("list_item_lang_nl")
    when(valueListCache.mapValueListItemId("add-legalArea", "aan")).thenReturn("list_item_add_legal_1")
    when(valueListCache.mapValueListItemId("add-legalArea", "vrm.verb")).thenReturn("list_item_add_legal_2")
    when(valueListCache.mapValueListItemId("rechtsthema", "AAN2")).thenReturn("list_item_rechtsthema")
    when(valueListCache.mapValueListItemId("publicationName", "c-aan")).thenReturn("list_item_publ_name")
  }

  private def mockFolderMapping(): Unit = {
    mockCache()
    when(valueListCache.mapValueListItemId("legalArea", "aan")).thenReturn("list_item_add_legal_1")
    when(valueListCache.mapValueListItemId("legalArea", "vrm.verb")).thenReturn("list_item_add_legal_2")
    when(valueListCache.mapValueListItemId("dc-type", "cmt")).thenReturn("list_item_type")
    when(valueListCache.mapValueListItemId("isPartOf", "sdu-commentaar")).thenReturn("list_item_isPartOf")
    when(valueListCache.mapValueListItemId("audience", "specialist")).thenReturn("list_item_audience")
  }

  private def mockCache(): Unit = {
    cut.metadataDefCache should be(empty)
    when(metadataApi.getMetadataDefinitions(documentTypeAuthor, NodeTypes.Document)).thenReturn(mockedJsonResp("metadata-comment-document"))
    when(metadataApi.getMetadataDefinitions(documentTypeAuthor, NodeTypes.Folder)).thenReturn(mockedJsonResp("metadata-comment-folder"))
    Await.result(cut.initCache(cacheValueListTypes), 10.seconds)
    cut.metadataDefCache should have size 2
    cut.metadataDefCache(cacheAuthorDocKey) should have size 9
    cut.metadataDefCache(cacheAuthorFolderKey) should have size 4
  }

}
