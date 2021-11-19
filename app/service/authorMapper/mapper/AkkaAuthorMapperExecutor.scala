package service.authorMapper.mapper

import akka.actor.ActorSystem
import akka.stream.IOResult
import akka.stream.scaladsl._
import akka.util.ByteString
import com.github.tototoshi.csv.CSVWriter
import components.publishone.AccessTokenHandler
import components.sws.{SwsApi, SwsSourceApi}
import play.api.Logger
import service.authorMapper.model.{Author, AuthorFolder}

import java.io.{ByteArrayInputStream, PrintWriter, StringWriter}
import java.nio.file.StandardOpenOption.{APPEND, CREATE, WRITE}
import java.nio.file.{Path, Paths}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AkkaAuthorMapperExecutor(swsSourceApi: SwsSourceApi,
                               swsApi: SwsApi,
                               accessTokenHandler: AccessTokenHandler,
                               authorFolderMapper: AuthorFolderMapper) {

  private lazy val log = Logger(getClass)
  private implicit val system: ActorSystem = ActorSystem("BackpressureBasics")
  private val mappedAuthors = new java.util.concurrent.ConcurrentHashMap[String, Boolean]

  def map(swsQuery: String): Unit = {
    log.info(s"Mapping authors for SWS query $swsQuery ...")
    val file = Paths.get("author-mapping.csv")
    writeCsvHeader(file)
    for {
      _ <- accessTokenHandler.accessToken
      _ <- map(swsQuery, file)
    } yield true
  }

  private def map(swsQuery: String, file: Path) = {
    val parallelism = 4
    val start = System.currentTimeMillis()
    val source = swsSourceApi.searchAndStreamDocs(swsQuery)
    val flow = source
      .mapAsyncUnordered(parallelism)(fetchDocument)
      .map(extractAuthors)
      .flatMapConcat(Source(_))
      .filter(validNotAlreadyMappedAuthor)
      .mapAsyncUnordered(parallelism)(authorFolderMapper.map)
      .map(toCsvRow)
      .runWith(FileIO.toPath(file, Set(WRITE, APPEND, CREATE)))
    onFlowComplete(start, flow)
    flow
  }

  private def onFlowComplete(start: Long, flow: Future[IOResult]): Unit = {
    flow.onComplete(_ => {
      val duration = (System.currentTimeMillis() - start) / 1000
      val mappedAuthorsCount = mappedAuthors.values().stream().filter(_ == true).count()
      val nonMappedAuthorsCount = mappedAuthors.size() - mappedAuthorsCount
      log.info(
        s"Mapping authors for SWS query done in $duration s" +
          s"\nTotal authors processed: ${mappedAuthors.size}" +
          s"\nMapped authors count: $mappedAuthorsCount" +
          s"\nNon mapped authors count: $nonMappedAuthorsCount")
    })
  }

  private def writeCsvHeader(file: Path): Unit = {
    val outWriter = new PrintWriter(file.toFile)
    outWriter.write(csvRow(Seq("identifier", "name", "familyName", "givenName", "initials", "document identifier", "folder id", "folder title")))
    outWriter.close()
  }

  private def fetchDocument(docKey: String) = {
    log.info(s"Fetching document $docKey ...")
    swsApi.getMetaXml(docKey).map((docKey, _))
  }

  private def extractAuthors(docKeyAndContent: (String, Array[Byte])): Seq[Author] = {
    log.info(s"Extracting authors for document ${docKeyAndContent._1} ....")
    val atomXml = xml.XML.load(new ByteArrayInputStream(docKeyAndContent._2))
    (atomXml \ "creator").map(Author(_, docKeyAndContent._1))
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

  private def toCsvRow(authorAndFolder: (Author, Option[AuthorFolder])) = {
    authorAndFolder match {
      case (author, folder) =>
        if (folder.isDefined) mappedAuthors.replace(author.identifier, true)
        val authorFields = author.productIterator.toSeq
        val folderFields = folder.map(_.productIterator.toSeq).getOrElse(Seq.empty)
        val allFields = Seq(authorFields, folderFields).flatten
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
