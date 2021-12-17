package service.authormapper.mapper

import components.publishone.NodeApi
import helpers.ScalaSpec
import helpers.TestUtils.mockedJsonResp
import org.mockito.Mockito.{reset, when}
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import service.authormapper.model.{Author, AuthorFolder}
import util.CreationStatus

import scala.concurrent.Await

class AuthorFolderMapperSpec extends ScalaSpec {

  lazy val author: Author = Author("J133", "E.S. de Jong", "Jong", "Eva", "E.S.", "de", "mr.", "f", "c-ar", "c-AR-W3936-610")
  lazy val folder: AuthorFolder = AuthorFolder(CreationStatus.Existing, 44462, "Jong, E.S. de", CreationStatus.Existing, "8857")

  val nodeApi: NodeApi = mock[NodeApi]
  val cut = new AuthorFolderMapper(nodeApi)

  override def beforeEach(): Unit = {
    reset(nodeApi)
  }

  "when there is single folder for family and given name then return it" in {
    when(nodeApi.getNodesByFamilyGivenNameInitials(author.familyName, author.givenName, null))
      .thenReturn(mockedJsonResp("nodes-by-metadata-single-folder"))
    when(nodeApi.getNodeMetadata(44462)).thenReturn(mockedJsonResp("metadata-author-folder-jong"))
    val result = Await.result(cut.map(author), 10.seconds)
    result should be(Some(folder))
  }

  "when there is no folder for family and given name then return None" in {
    when(nodeApi.getNodesByFamilyGivenNameInitials(author.familyName, author.givenName, null))
      .thenReturn(mockedJsonResp("empty-list"))
    when(nodeApi.getNodesByFamilyGivenNameInitials(author.familyName, null, null))
      .thenReturn(mockedJsonResp("empty-list"))
    val result = Await.result(cut.map(author), 10.seconds)
    result should be(None)
  }

  "when there is several folders for family and given name and filter by family and given name and initials finds one result then return it" in {
    when(nodeApi.getNodesByFamilyGivenNameInitials(author.familyName, author.givenName, null))
      .thenReturn(mockedJsonResp("nodes-by-metadata-several-folders"))
    when(nodeApi.getNodeMetadata(44460)).thenReturn(mockedJsonResp("metadata-author-folder-jong-fg"))
    when(nodeApi.getNodeMetadata(44461)).thenReturn(mockedJsonResp("metadata-author-folder-jong-fi"))
    when(nodeApi.getNodeMetadata(44462)).thenReturn(mockedJsonResp("metadata-author-folder-jong"))
    val result = Await.result(cut.map(author), 100.seconds)
    result should be(Some(folder))
  }

  "when there is several folders for family and given name and filter by family and given name finds one result then return it" in {
    when(nodeApi.getNodesByFamilyGivenNameInitials(author.familyName, author.givenName, null))
      .thenReturn(mockedJsonResp("nodes-by-metadata-several-folders"))
    when(nodeApi.getNodeMetadata(44460)).thenReturn(mockedJsonResp("metadata-author-folder-jong-none"))
    when(nodeApi.getNodeMetadata(44461)).thenReturn(mockedJsonResp("metadata-author-folder-jong-fi"))
    when(nodeApi.getNodeMetadata(44462)).thenReturn(mockedJsonResp("metadata-author-folder-jong-fg"))
    val result = Await.result(cut.map(author), 100.seconds)
    result should be(Some(folder.copy(authorItemId = "9001")))
  }

  "when there is several folders for family and given name and filter by family name and initials finds one result then return it" in {
    when(nodeApi.getNodesByFamilyGivenNameInitials(author.familyName, author.givenName, null))
      .thenReturn(mockedJsonResp("nodes-by-metadata-several-folders"))
    when(nodeApi.getNodeMetadata(44460)).thenReturn(mockedJsonResp("metadata-author-folder-jong-none"))
    when(nodeApi.getNodeMetadata(44461)).thenReturn(mockedJsonResp("metadata-author-folder-jong-none"))
    when(nodeApi.getNodeMetadata(44462)).thenReturn(mockedJsonResp("metadata-author-folder-jong-fi"))
    val result = Await.result(cut.map(author), 100.seconds)
    result should be(Some(folder.copy(authorItemId = "9002")))
  }

}
