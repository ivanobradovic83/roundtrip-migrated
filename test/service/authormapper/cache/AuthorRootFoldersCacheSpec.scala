package service.authormapper.cache

import components.publishone.NodeApi
import helpers.ScalaSpec
import helpers.TestUtils._
import org.mockito.Mockito.{reset, when}
import util.ConfigUtils

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class AuthorRootFoldersCacheSpec extends ScalaSpec {

  lazy val pageNumber: Int = 1
  lazy val pageSize: Int = 100

  val configUtils: ConfigUtils = mock[ConfigUtils]
  val nodeApi: NodeApi = mock[NodeApi]
  val cut = new AuthorRootFoldersCache(configUtils, nodeApi)

  override def beforeEach(): Unit = {
    reset(configUtils)
    reset(nodeApi)
    cut.cleanCache()
  }

  "when root folder id not defined then do not init cache" in {
    when(configUtils.publishOneAuthorsRootFolderId).thenReturn(-1)
    Await.result(cut.initCache, 10.seconds)
    cut.rootFoldersCache should be(empty)
  }

  "when cache initialized it should contain valid data" in {
    mockData

    Await.result(cut.initCache, 10.seconds)

    cut.rootFoldersCache should contain only ("a" -> 43023, "b" -> 43024, "z" -> 43033, "aa" -> 43026, "ab" -> 43027, "zo" -> 44094)

    cut.cleanCache()
    cut.rootFoldersCache should be(empty)
  }

  private def mockData = {
    cut.rootFoldersCache should be(empty)
    when(configUtils.publishOneAuthorsRootFolderId).thenReturn(1)
    when(nodeApi.getChildNodes(1, pageNumber, pageSize)).thenReturn(mockedJsonResp("root-folders-1"))
    when(nodeApi.getChildNodes(43023, pageNumber, pageSize)).thenReturn(mockedJsonResp("root-folders-43023"))
    when(nodeApi.getChildNodes(43024, pageNumber, pageSize)).thenReturn(mockedJsonResp("root-folders-43024"))
    when(nodeApi.getChildNodes(43033, pageNumber, pageSize)).thenReturn(mockedJsonResp("root-folders-43033"))
  }

}
