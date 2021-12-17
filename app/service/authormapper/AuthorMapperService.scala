package service.authormapper

import akka.actor.ActorSystem
import akka.stream.ActorAttributes.withSupervisionStrategy
import akka.stream.Supervision.resumingDecider
import akka.stream.scaladsl._
import akka.util.ByteString
import com.github.tototoshi.csv.CSVWriter
import components.publishone.AccessTokenHandler
import components.sws.{SwsApi, SwsSourceApi}
import play.api.Logger
import service.authormapper.cache.{AuthorListItemsCache, AuthorRootFoldersCache}
import service.authormapper.mapper.{AuthorDocumentCreator, AuthorDocumentMapper, AuthorFolderCreator, AuthorFolderMapper}
import service.authormapper.model.{Author, AuthorDocument, AuthorFolder}
import service.common.cache.ValueListCache
import service.common.monithoring.InProgressHandler
import util.{ConfigUtils, NodeTypes}
import util.PublishOneConstants.documentTypeAuthor

import java.io.{ByteArrayInputStream, PrintWriter, StringWriter}
import java.nio.file.StandardOpenOption.{APPEND, CREATE, WRITE}
import java.nio.file.{Path, Paths}
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import javax.inject.Inject
import scala.util.{Failure, Success, Try}

/** This service creates and runs Akka streams flow which creates mapping file between author (from SWS document metadata) and PublishOne
  * folder/document
  *
  * @param swsSourceApi
  *   Akka source from SWS query
  * @param swsApi
  *   SWS API
  * @param accessTokenHandler
  *   PublishOne access token handler
  * @param authorFolderMapper
  *   PublishOne author folder mapper
  * @param authorFolderCreator
  *   PublishOne author folder creator
  * @param authorDocumentCreator
  *   PublishOne author document creator
  * @param valueListCache
  *   PublishOne cache
  */
class AuthorMapperService @Inject()(
    configUtils: ConfigUtils,
    inProgressHandler: InProgressHandler,
    swsSourceApi: SwsSourceApi,
    swsApi: SwsApi,
    accessTokenHandler: AccessTokenHandler,
    authorFolderMapper: AuthorFolderMapper,
    authorFolderCreator: AuthorFolderCreator,
    authorDocumentMapper: AuthorDocumentMapper,
    authorDocumentCreator: AuthorDocumentCreator,
    valueListCache: ValueListCache,
    authorRootFoldersCache: AuthorRootFoldersCache,
    authorListItemsCache: AuthorListItemsCache
) {

  type AuthorFolderDoc = (Author, Option[AuthorFolder], Option[AuthorDocument])

  private lazy val log = Logger(getClass)
  private implicit val system: ActorSystem = ActorSystem("AuthorMappingSystem")
  private val mappedAuthorsCache = new java.util.concurrent.ConcurrentHashMap[String, Boolean]
  private val mappedFoldersCounter = new AtomicInteger(0)
  private val mappedDocumentsCounter = new AtomicInteger(0)
  private lazy val cacheValueListTypes = Seq(
    documentTypeAuthor -> NodeTypes.Document,
    documentTypeAuthor -> NodeTypes.Folder
  )
  private lazy val csvFilePath = Paths.get(configUtils.publishOneAuthorMappingFile)

  def map(swsQuery: String, createMissingDocuments: Boolean): Unit = {
    log.info(s"Mapping authors for SWS query $swsQuery ...")

    val start = System.currentTimeMillis()
    initAuthorsMapping
      .flatMap(_ => runMappingFlow(swsQuery, createMissingDocuments))
      .onComplete(onMappingFlowComplete(_, start))
  }

  private def initAuthorsMapping: Future[Unit] = {
    inProgressHandler.startProcess(getClass.getSimpleName)
    writeCsvHeader(csvFilePath)
    for {
      _ <- accessTokenHandler.accessToken
      _ <- valueListCache.initCache(cacheValueListTypes) zip authorRootFoldersCache.initCache
      _ <- authorListItemsCache.initCache()
    } yield ()
  }

  private def runMappingFlow(swsQuery: String, createMissingDocuments: Boolean) = {
    var flow = swsSourceApi
      .searchAndStreamDocs(swsQuery)
      .mapAsyncUnordered(configUtils.parallelism)(fetchDocument)
      .map(extractAuthors)
      .flatMapConcat(Source(_))
      .filter(validNotAlreadyMappedAuthor)
      .async
      .mapAsyncUnordered(configUtils.parallelism)(mapAuthorFolder)
      .mapAsyncUnordered(configUtils.parallelism)(mapAuthorDocument)
      .async
    if (createMissingDocuments) {
      flow = flow
        .mapAsyncUnordered(configUtils.parallelism)(createAuthorFolderIfMissing)
        .mapAsyncUnordered(configUtils.parallelism)(createAuthorDocumentIfMissing)
        .async
    }
    flow
      .map(toCsvRow)
      .withAttributes(withSupervisionStrategy(resumingDecider))
      .runWith(FileIO.toPath(csvFilePath, Set(WRITE, APPEND, CREATE)))
  }

  private def writeCsvHeader(file: Path): Unit = {
    val outWriter = new PrintWriter(file.toFile)
    outWriter.write(
      csvRow(
        Seq(
          "identifier",
          "name",
          "familyName",
          "givenName",
          "initials",
          "familyNamePrefix",
          "prefix",
          "gender",
          "publicationName",
          "sws document",
          "folder status",
          "folder id",
          "folder title",
          "author item status",
          "author item id",
          "document status",
          "document id",
          "document title"
        )
      )
    )
    outWriter.close()
  }

  private def fetchDocument(docKey: String) = {
    log.info(s"Fetching document $docKey ...")
    swsApi.getMetaXml(docKey).map((docKey, _)).recover(recover("getMetaXml", docKey))
  }

  private def extractAuthors(docKeyAndContent: (String, Array[Byte])): Seq[Author] = {
    log.info(s"Extracting authors for document ${docKeyAndContent._1} ....")
    val atomXml = xml.XML.load(new ByteArrayInputStream(docKeyAndContent._2))
    val publicationName = (atomXml \ "published" \ "publicationName" \ "identifier").text.toLowerCase
    (atomXml \ "creator").map(Author(_, publicationName, docKeyAndContent._1))
  }

  private def validNotAlreadyMappedAuthor(author: Author): Boolean = {
    if ("" == author.familyName) {
      log.warn(s"invalid author $author")
      false
    } else if (!mappedAuthorsCache.containsKey(author.identifier)) {
      log.info(s"$author not mapped yet")
      mappedAuthorsCache.put(author.identifier, false)
      true
    } else {
      log.info(s"$author already mapped")
      false
    }
  }

  private def mapAuthorFolder(author: Author) =
    authorFolderMapper
      .map(author)
      .map((author, _))
      .recover(recover("mapAuthorFolder", author.toString))

  private def mapAuthorDocument(authorAndFolder: (Author, Option[AuthorFolder])) = {
    if (authorAndFolder._2.nonEmpty)
      authorDocumentMapper
        .map(authorAndFolder._1, authorAndFolder._2.get)
        .map((authorAndFolder._1, authorAndFolder._2, _))
        .recover(recover("mapAuthorDocument", authorAndFolder.toString))
    else Future.successful((authorAndFolder._1, authorAndFolder._2, Option.empty[AuthorDocument]))
  }

  private def createAuthorFolderIfMissing(authorFolderDoc: AuthorFolderDoc): Future[AuthorFolderDoc] = {
    authorFolderDoc match {
      case (author, folder, document) if folder.isEmpty =>
        authorFolderCreator
          .create(author)
          .map(newFolder => (author, Some(newFolder), document))
          .recover(recover("createAuthorFolderIfMissing", authorFolderDoc.toString))
      case _ => Future.successful(authorFolderDoc)
    }
  }

  private def createAuthorDocumentIfMissing(authorFolderDoc: AuthorFolderDoc): Future[AuthorFolderDoc] = {
    authorFolderDoc match {
      case (author, folder, document) if document.isEmpty =>
        authorDocumentCreator
          .create(author, folder.get)
          .map(newDocument => (author, folder, Option(newDocument)))
          .recover(recover("createAuthorDocumentIfMissing", authorFolderDoc.toString))
      case _ => Future.successful(authorFolderDoc)
    }
  }

  private def toCsvRow(authorAndFolder: AuthorFolderDoc) = {
    authorAndFolder match {
      case (author, folder, document) =>
        if (folder.isDefined) mappedFoldersCounter.addAndGet(1)
        if (document.isDefined) mappedDocumentsCounter.addAndGet(1)
        val authorFields = author.productIterator.toSeq
        val folderFields = folder.map(_.productIterator.toSeq).getOrElse(Seq.empty)
        val documentFields = document.map(_.productIterator.toSeq).getOrElse(Seq.empty)
        val allFields = Seq(authorFields, folderFields, documentFields).flatten
        ByteString(csvRow(allFields))
    }
  }

  private def csvRow(fields: Seq[Any]): String = {
    val sw = new StringWriter()
    val csv = CSVWriter.open(sw)
    csv.writeRow(fields)
    sw.toString
  }

  private def recover[T](flowInfo: String, additionalInfo: String): PartialFunction[Throwable, T] = {
    case e: Throwable =>
      log.error(s"$flowInfo $additionalInfo: ${e.getMessage}")
      throw e
  }

  private def onMappingFlowComplete[A](result: Try[A], start: Long): Unit = {
    val duration = (System.currentTimeMillis() - start) / 1000
    val nonMappedAuthorsCount = mappedAuthorsCache.size() - mappedFoldersCounter.get
    val message = s"\nMapping authors for SWS query done in $duration s" +
      s"\nTotal authors processed: ${mappedAuthorsCache.size}" +
      s"\nMapped author folders/documents count: $mappedFoldersCounter / $mappedDocumentsCounter" +
      s"\nNon mapped authors count: $nonMappedAuthorsCount"
    result match {
      case Failure(exception) => log.error(s"Mapping failed $message", exception)
      case Success(_)         => log.info(s"Mapping done successfully $message")
    }
    closeMappingProcess()
  }

  private def closeMappingProcess(): Unit = {
    inProgressHandler.stopProcess(getClass.getSimpleName)
    mappedAuthorsCache.clear()
    mappedFoldersCounter.set(0)
    mappedDocumentsCounter.set(0)
    valueListCache.cleanCache()
    authorRootFoldersCache.cleanCache()
    authorListItemsCache.cleanCache()
  }

}
