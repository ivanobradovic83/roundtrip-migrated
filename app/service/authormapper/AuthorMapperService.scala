package service.authormapper

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.util.ByteString
import com.github.tototoshi.csv.CSVWriter
import components.publishone.AccessTokenHandler
import components.sws.{SwsApi, SwsSourceApi}
import play.api.Logger
import service.authormapper.cache.PublishOneCache
import service.authormapper.mapper.{AuthorDocumentCreator, AuthorFolderCreator, AuthorFolderMapper}
import service.authormapper.model.{Author, AuthorDocument, AuthorFolder}

import java.io.{ByteArrayInputStream, PrintWriter, StringWriter}
import java.nio.file.StandardOpenOption.{APPEND, CREATE, WRITE}
import java.nio.file.{Path, Paths}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import javax.inject.Inject

/**
  *
  * This service creates and runs Akka streams flow which creates mapping file between
  * author (from SWS document metadata) and PublishOne folder/document
  *
  * @param swsSourceApi Akka source from SWS query
  * @param swsApi SWS API
  * @param accessTokenHandler PublishOne access token handler
  * @param authorFolderMapper PublishOne author folder mapper
  * @param authorFolderCreator PublishOne author folder creator
  * @param authorDocumentCreator PublishOne author document creator
  * @param publishOneCache PublishOne cache
  */
class AuthorMapperService @Inject()(swsSourceApi: SwsSourceApi,
                                    swsApi: SwsApi,
                                    accessTokenHandler: AccessTokenHandler,
                                    authorFolderMapper: AuthorFolderMapper,
                                    authorFolderCreator: AuthorFolderCreator,
                                    authorDocumentCreator: AuthorDocumentCreator,
                                    publishOneCache: PublishOneCache) {

  private lazy val log = Logger(getClass)
  private implicit val system: ActorSystem = ActorSystem("BackpressureBasics")
  private val mappedAuthors = new java.util.concurrent.ConcurrentHashMap[String, Boolean]

  def map(swsQuery: String, createMissingDocuments: Boolean): Unit = {
    log.info(s"Mapping authors for SWS query $swsQuery ...")
    val file = Paths.get("author-mapping.csv")
    writeCsvHeader(file)

    val start = System.currentTimeMillis()
    beforeMappingFlow
      .flatMap(_ => runMappingFlow(swsQuery, createMissingDocuments, file))
      .onComplete(_ => mappingFlowComplete(start))
  }

  private def beforeMappingFlow = accessTokenHandler.accessToken zip publishOneCache.initCache

  private def runMappingFlow(swsQuery: String, createMissingDocuments: Boolean, file: Path) = {
    val parallelism = 4
    swsSourceApi
      .searchAndStreamDocs(swsQuery)
      .mapAsyncUnordered(parallelism)(fetchDocument)
      .map(extractAuthors)
      .flatMapConcat(Source(_))
      .filter(validNotAlreadyMappedAuthor)
      .mapAsyncUnordered(parallelism)(authorFolderMapper.map)
      .mapAsyncUnordered(parallelism)(createAuthorFolderIfMissing(_, createMissingDocuments))
      .mapAsyncUnordered(parallelism)(createAuthorDocumentIfMissing(_, createMissingDocuments))
      .map(toCsvRow)
      .runWith(FileIO.toPath(file, Set(WRITE, APPEND, CREATE)))
  }

  private def mappingFlowComplete(start: Long): Unit = {
    val duration = (System.currentTimeMillis() - start) / 1000
    val mappedAuthorsCount = mappedAuthors.values().stream().filter(_ == true).count()
    val nonMappedAuthorsCount = mappedAuthors.size() - mappedAuthorsCount
    log.info(
      s"Mapping authors for SWS query done in $duration s" +
        s"\nTotal authors processed: ${mappedAuthors.size}" +
        s"\nMapped authors count: $mappedAuthorsCount" +
        s"\nNon mapped authors count: $nonMappedAuthorsCount")
    mappedAuthors.clear()
    publishOneCache.cleanCache()
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
          "folder id",
          "folder title",
          "document id",
          "document title"
        )))
    outWriter.close()
  }

  private def fetchDocument(docKey: String) = {
    log.info(s"Fetching document $docKey ...")
    swsApi.getMetaXml(docKey).map((docKey, _))
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
    } else if (!mappedAuthors.containsKey(author.identifier)) {
      log.info(s"$author not mapped yet")
      mappedAuthors.put(author.identifier, false)
      true
    } else {
      log.info(s"$author already mapped")
      false
    }
  }

  private def createAuthorFolderIfMissing(authorAndFolder: (Author, Option[AuthorFolder]),
                                          createMissingDocuments: Boolean): Future[(Author, Option[AuthorFolder])] = {
    if (createMissingDocuments && authorAndFolder._2.isEmpty)
      authorFolderCreator
        .create(authorAndFolder._1)
        .map(folder => (authorAndFolder._1, Option(folder)))
    else Future.successful(authorAndFolder)
  }

  private def createAuthorDocumentIfMissing(authorAndFolder: (Author, Option[AuthorFolder]),
                                            createMissingDocuments: Boolean): Future[(Author, Option[AuthorFolder], Option[AuthorDocument])] = {
    if (createMissingDocuments) {
      authorDocumentCreator
        .create(authorAndFolder._1, authorAndFolder._2.get)
        .map(document => (authorAndFolder._1, authorAndFolder._2, Option(document)))
    } else Future.successful((authorAndFolder._1, authorAndFolder._2, Option.empty[AuthorDocument]))
  }

  private def toCsvRow(authorAndFolder: (Author, Option[AuthorFolder], Option[AuthorDocument])) = {
    authorAndFolder match {
      case (author, folder, document) =>
        if (folder.isDefined) mappedAuthors.replace(author.identifier, true)
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

}
