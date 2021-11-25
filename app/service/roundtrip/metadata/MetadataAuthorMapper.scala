package service.roundtrip.metadata

import com.github.tototoshi.csv.CSVReader
import service.roundtrip.model.AuthorDocumentMapping
import util.StringUtils.notEmpty

import javax.inject.Inject
import scala.collection.concurrent.TrieMap

/**
  * This class is using <b>author-mapping.csv</b> document, generated by [[service.authormapper.AuthorMapperService]],
  * to map given SWS document identifier to author document in PublishOne
  */
class MetadataAuthorMapper @Inject()() {

  private lazy val swsAuthorIdIndex: Int = 0
  private lazy val p1AuthorDocIdIndex: Int = 12
  private lazy val p1AuthorDocNameIndex: Int = 13
  private lazy val p1AuthorItemIdIndex: Int = 14
  private lazy val authorDocumentMappingCache: TrieMap[String, AuthorDocumentMapping] = new TrieMap[String, AuthorDocumentMapping]()

  def mapAuthorToDocument(swsAuthorId: String): Option[AuthorDocumentMapping] = authorDocumentMappingCache.get(swsAuthorId)

  def initCache(): Unit = {
    cleanCache()
    val csv = CSVReader.open("author-mapping.csv")
    csv.toStream.tail.foreach { row =>
      val swsAuthorId = row(swsAuthorIdIndex)
      val p1AuthorDocId = row(p1AuthorDocIdIndex)
      val p1AuthorDocName = row(p1AuthorDocNameIndex)
      val p1AuthorItemId = if (row.size > p1AuthorItemIdIndex) row(p1AuthorItemIdIndex) else ""
      if (notEmpty(p1AuthorDocId) && notEmpty(p1AuthorDocName))
        authorDocumentMappingCache.put(swsAuthorId, AuthorDocumentMapping(p1AuthorDocId.toInt, p1AuthorDocName, p1AuthorItemId))
    }
  }

  def cleanCache(): Unit = authorDocumentMappingCache.clear()

}
