package service.roundtrip.metadata

import helpers.ScalaSpec
import org.mockito.Mockito.when
import service.roundtrip.model.AuthorDocumentMapping
import util.ConfigUtils

import java.io.FileNotFoundException
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class AuthorDocumentMapperSpec extends ScalaSpec {

  lazy val author1CwcId = "slautenbag"
  lazy val author2CwcId = "tdankert"
  lazy val author1Id = 44505
  lazy val author2Id = 44506
  lazy val author1Title = "Lautenbag, S. (Commentaren)"
  lazy val author2Title = "Dankert, Th. (Commentaren)"
  lazy val author2ItemId = "8861"

  val configUtils: ConfigUtils = mock[ConfigUtils]
  val cut: AuthorDocumentMapper = new AuthorDocumentMapper(configUtils)

  override def beforeEach(): Unit = cut.cleanCache()

  "when cache author mapping file then verify data loaded successfully" in {
    when(configUtils.publishOneAuthorMappingFile).thenReturn("test/resources/author-mapping.csv")
    cut.authorDocumentMappingCache should be(empty)

    Await.result(cut.initCache, 10.seconds)

    cut.authorDocumentMappingCache.size should be(2)
    cut.authorDocumentMappingCache should contain(author1CwcId -> AuthorDocumentMapping(author1Id, author1Title, ""))
    cut.authorDocumentMappingCache should contain(author2CwcId -> AuthorDocumentMapping(author2Id, author2Title, author2ItemId))
  }

  "when clean author mapping cache then verify cache is empty" in {
    when(configUtils.publishOneAuthorMappingFile).thenReturn("test/resources/author-mapping.csv")
    cut.authorDocumentMappingCache should be(empty)

    Await.result(cut.initCache, 10.seconds)
    cut.authorDocumentMappingCache.size should be(2)

    cut.cleanCache()
    cut.authorDocumentMappingCache should be(empty)
  }

  "when cache missing author mapping file then throw exception" in {
    when(configUtils.publishOneAuthorMappingFile).thenReturn("test/resources/author-mapping-missing.csv")
    cut.authorDocumentMappingCache should be(empty)

    the[FileNotFoundException] thrownBy Await.result(cut.initCache, 10.seconds)
  }

  "when mapping CWC author id to PublishOne then return valid PublishOne document and value list item ids" in {
    when(configUtils.publishOneAuthorMappingFile).thenReturn("test/resources/author-mapping.csv")
    cut.authorDocumentMappingCache should be(empty)

    Await.result(cut.initCache, 10.seconds)

    cut.mapAuthorToDocument(author1CwcId) should be(Some(AuthorDocumentMapping(author1Id, author1Title, "")))
    cut.mapAuthorToDocument(author2CwcId) should be(Some(AuthorDocumentMapping(author2Id, author2Title, author2ItemId)))
    cut.mapAuthorToDocument("missing_id") should be(None)
  }

}
