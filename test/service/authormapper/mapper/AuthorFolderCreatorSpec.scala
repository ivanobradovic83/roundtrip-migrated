package service.authormapper.mapper

import components.publishone.{DocumentApi, FolderApi, NodeOperationApi}
import helpers.ScalaSpec
import helpers.TestUtils.mockedJsonResp
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import service.authormapper.cache.AuthorRootFoldersCache
import service.authormapper.model.{Author, AuthorDocument, AuthorFolder}
import service.common.cache.ValueListCache
import util.CreationStatus
import util.PublishOneConstants._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class AuthorFolderCreatorSpec extends ScalaSpec {

  lazy val author: Author = Author("J133", "E.S. de Jong", "Jong", "Eva", "E.S.", "de", "mr.", "f", "c-ar", "c-AR-W3936-610")
  lazy val folderName: String = "Jong, E.S. de"
  lazy val folderNameNoInitials: String = "Jong, de"
  lazy val folderNameNoFamilyNamePrefix: String = "Jong, E.S."
  lazy val folderNameNoInitialsNoFamilyNamePrefix: String = "Jong"
  lazy val oneLetterAuthorFolderId: Int = 5001
  lazy val twoLettersAuthorFolderId: Int = 5002
  lazy val twoLettersAuthorFolderAlreadyCreatedId: Int = 5003
  lazy val authorItemId: String = "6001"
  lazy val genderListItemId: String = "7001"
  lazy val prefixListItemId: String = "7002"
  lazy val familyNamePrefixListItemId: String = "7003"
  lazy val folder: AuthorFolder = AuthorFolder(CreationStatus.New, 4523, folderName, CreationStatus.New, authorItemId)
  lazy val folderMetadata = Map(
    "givenName" -> author.givenName,
    "familyName" -> author.familyName,
    "initials" -> author.initials,
    listItemsGender -> genderListItemId,
    listItemsPrefix -> prefixListItemId,
    listItemsFamilyNamePrefix -> familyNamePrefixListItemId,
    listItemsAuthor -> s"[$authorItemId]"
  )

  val folderApi: FolderApi = mock[FolderApi]
  val nodeOperationApi: NodeOperationApi = mock[NodeOperationApi]
  val valueListCache: ValueListCache = mock[ValueListCache]
  val authorRootFoldersCache: AuthorRootFoldersCache = mock[AuthorRootFoldersCache]
  val authorListItemsHandler: AuthorListItemsHandler = mock[AuthorListItemsHandler]
  val cut = new AuthorFolderCreator(folderApi, nodeOperationApi, valueListCache, authorRootFoldersCache, authorListItemsHandler)

  override def beforeEach(): Unit = {
    reset(folderApi)
    reset(nodeOperationApi)
    reset(valueListCache)
    reset(authorRootFoldersCache)
    reset(authorListItemsHandler)
  }

  "when valid author given and 2 letters root folder exists then create folder" in {
    when(valueListCache.mapValueListItemId(listItemsFamilyNamePrefix, author.familyNamePrefix)).thenReturn(familyNamePrefixListItemId)
    testAuthorFolderCreated(author, folderName, folderMetadata)
  }

  "when valid author (no initials) given and 2 letters root folder exists then create folder" in {
    val metadata = folderMetadata map {
      case ("initials", _) => "initials" -> ""
      case x               => x
    }
    when(valueListCache.mapValueListItemId(listItemsFamilyNamePrefix, author.familyNamePrefix)).thenReturn(familyNamePrefixListItemId)
    testAuthorFolderCreated(author.copy(initials = ""), folderNameNoInitials, metadata)
  }

  "when valid author (no family name prefix) given and 2 letters root folder exists then create folder" in {
    val metadata = folderMetadata map {
      case ("familyNamePrefix", _) => "familyNamePrefix" -> ""
      case x                       => x
    }
    when(valueListCache.mapValueListItemId(listItemsFamilyNamePrefix, "")).thenReturn("")
    testAuthorFolderCreated(author.copy(familyNamePrefix = ""), folderNameNoFamilyNamePrefix, metadata)
  }

  "when valid author (no family name prefix, no initials) given and 2 letters root folder exists then create folder" in {
    val metadata = folderMetadata map {
      case ("familyNamePrefix", _) => "familyNamePrefix" -> ""
      case ("initials", _)         => "initials" -> ""
      case x                       => x
    }
    when(valueListCache.mapValueListItemId(listItemsFamilyNamePrefix, "")).thenReturn("")
    testAuthorFolderCreated(author.copy(familyNamePrefix = "", initials = ""), folderNameNoInitialsNoFamilyNamePrefix, metadata)
  }

  "when valid author given and 1 letter root folder exists then create folder" in {
    when(authorRootFoldersCache.getRootFolderId("jo")).thenReturn(None)
    when(authorRootFoldersCache.getRootFolderId("j")).thenReturn(Some(oneLetterAuthorFolderId))
    when(folderApi.createFolder(oneLetterAuthorFolderId, "Jo", documentTypeAuthor))
      .thenReturn(mockedJsonResp("create-author-root-folder-2letters"))
    when(authorRootFoldersCache.addOrGetCachedValue("jo", twoLettersAuthorFolderId)).thenReturn(twoLettersAuthorFolderId)
    when(authorListItemsHandler.findOrCreate(folderName)).thenReturn(Future.successful(authorItemId))
    when(valueListCache.mapValueListItemId(listItemsGender, author.gender)).thenReturn(genderListItemId)
    when(valueListCache.mapValueListItemId(listItemsPrefix, author.prefix)).thenReturn(prefixListItemId)
    when(valueListCache.mapValueListItemId(listItemsFamilyNamePrefix, author.familyNamePrefix)).thenReturn(familyNamePrefixListItemId)
    when(folderApi.createFolder(twoLettersAuthorFolderId, folderName, documentTypeAuthor, folderMetadata)).thenReturn(mockedJsonResp("create-folder"))

    val result = Await.result(cut.create(author), 10.seconds)
    result should be(folder)
  }

  "when valid author given and 2 letters root folder created at the same time by another future then delete it" in {
    when(authorRootFoldersCache.getRootFolderId("jo")).thenReturn(None)
    when(authorRootFoldersCache.getRootFolderId("j")).thenReturn(Some(oneLetterAuthorFolderId))
    when(folderApi.createFolder(oneLetterAuthorFolderId, "Jo", documentTypeAuthor))
      .thenReturn(mockedJsonResp("create-author-root-folder-2letters"))
    when(authorRootFoldersCache.addOrGetCachedValue("jo", twoLettersAuthorFolderId)).thenReturn(twoLettersAuthorFolderAlreadyCreatedId)
    when(authorListItemsHandler.findOrCreate(folderName)).thenReturn(Future.successful(authorItemId))
    when(valueListCache.mapValueListItemId(listItemsGender, author.gender)).thenReturn(genderListItemId)
    when(valueListCache.mapValueListItemId(listItemsPrefix, author.prefix)).thenReturn(prefixListItemId)
    when(valueListCache.mapValueListItemId(listItemsFamilyNamePrefix, author.familyNamePrefix)).thenReturn(familyNamePrefixListItemId)
    when(folderApi.createFolder(twoLettersAuthorFolderAlreadyCreatedId, folderName, documentTypeAuthor, folderMetadata))
      .thenReturn(mockedJsonResp("create-folder"))

    val result = Await.result(cut.create(author), 10.seconds)
    result should be(folder)
    verify(nodeOperationApi).deleteNode(twoLettersAuthorFolderId, includeDescendants = true)
  }

  "when valid author given and 1 letter root folder not exists then throw exception" in {
    when(authorRootFoldersCache.getRootFolderId("jo")).thenReturn(None)
    when(authorRootFoldersCache.getRootFolderId("j")).thenReturn(None)

    val exception = the[Exception] thrownBy Await.result(cut.create(author), 10.seconds)
    exception.getMessage should be("There is no author folder j")
  }

  private def testAuthorFolderCreated(testAuthor: Author, testFolderName: String, testFolderMetadata: Map[String, String]): Unit = {
    when(authorRootFoldersCache.getRootFolderId("jo")).thenReturn(Some(twoLettersAuthorFolderId))
    when(authorListItemsHandler.findOrCreate(testFolderName)).thenReturn(Future.successful(authorItemId))
    when(valueListCache.mapValueListItemId(listItemsGender, testAuthor.gender)).thenReturn(genderListItemId)
    when(valueListCache.mapValueListItemId(listItemsPrefix, testAuthor.prefix)).thenReturn(prefixListItemId)
    when(folderApi.createFolder(twoLettersAuthorFolderId, testFolderName, documentTypeAuthor, testFolderMetadata))
      .thenReturn(mockedJsonResp("create-folder"))

    val result = Await.result(cut.create(testAuthor), 10.seconds)
    result should be(folder)
  }

}
