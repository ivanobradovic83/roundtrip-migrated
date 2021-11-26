package service.roundtrip.metadata

import components.publishone.MetadataApi
import dto.RoundTripDto
import play.api.Logger
import util.NodeTypes.NodeType
import util.PublishOneConstants._
import play.api.libs.json._
import service.roundtrip.model.AuthorDocumentMapping

import java.io.ByteArrayInputStream
import javax.inject.Inject
import scala.collection.immutable.Iterable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.Node

/**
  * This class maps given metadata in XML format to string values which will be used as Json values by executing next steps:
  * <pre>
  * - parse given metadataXml
  * - traverse through all Xml metadata
  * - maps values from XML to Json value
  * - returns Map of Json metadata values which are ready to be used with PublishOne API
  * </pre>
  *
  * @param metadataApi PublishOne Metadata API
  */
class MetadataMapper @Inject()(metadataApi: MetadataApi, metadataAuthorMapper: MetadataAuthorMapper) {

  private lazy val log = Logger(getClass)

  def mapXmlMetadata(roundTripDto: RoundTripDto, metadataXmlContent: Array[Byte], nodeType: NodeType): Future[Map[String, AnyRef]] = {
    log.info(s"$roundTripDto Map $nodeType XML metadata to Json started")
    for {
      metadataDefs <- getMetadataDefinitions(roundTripDto.docType, nodeType)
      metadataJson <- mapXmlToJsonMetadata(metadataXmlContent, metadataDefs)
      _ <- Future.successful(log.info(s"$roundTripDto $nodeType XML metadata mapped to Json"))
    } yield metadataJson
  }

  private def mapXmlToJsonMetadata(metadataXmlContent: Array[Byte], metadataDefs: Seq[JsValue]): Future[Map[String, AnyRef]] =
    getJsonMetadataValues(metadataXmlContent, metadataDefs)
      .map(filterNonEmpty)
      .map(_.toMap)

  private def getJsonMetadataValues(metadataXmlContent: Array[Byte], metadataDefs: Seq[JsValue]): Future[Seq[(String, AnyRef)]] = {
    val metadataXmlChildren = xml.XML.load(new ByteArrayInputStream(metadataXmlContent)).child
    val getJsonMetadataValueFutures = metadataDefs.map { metadataDef =>
      val metadataDefName = (metadataDef \ "name").as[String]
      metadataXmlChildren.find(_.label == metadataDefName) match {
        case Some(metadataValue) => getJsonMetadataValue(metadataDefName, metadataDef, metadataValue)
        case None                => Future.successful(metadataDefName, null)
      }
    }
    Future.sequence(getJsonMetadataValueFutures)
  }

  private def getJsonMetadataValue(metadataName: String, metadataDef: JsValue, metadataXmlChild: Node): Future[(String, AnyRef)] = {
    val getMetadataValue: Future[AnyRef] = getMetadataType(metadataDef) match {
      case "selectList" if metadataName == listItemsAuthor => handleAuthors(metadataXmlChild)
      case "selectList" if isMultiSelectList(metadataDef)  => handleMultiSelectList(metadataXmlChild, metadataDef)
      case "selectList"                                    => handleSingleSelectList(metadataXmlChild, metadataDef)
      case "multipleStringValue"                           => handleMultipleStringValue(metadataXmlChild)
      case _                                               => Future.successful(metadataXmlChild.text)
    }
    getMetadataValue.map((metadataName, _))
  }

  private def filterNonEmpty(jsonMetadataValues: Seq[(String, AnyRef)]): Seq[(String, AnyRef)] =
    jsonMetadataValues.filter(_._2 != null)

  private def handleSingleSelectList(metadataXmlChild: Node, metadataDef: JsValue): Future[String] = {
    val itemKey = metadataXmlChild.attribute("key").map(keyNode => keyNode.text).head
    getAndMapValueListItemKeysToIds(metadataDef, true, itemKey)
  }

  private def handleMultiSelectList(metadataXmlChild: Node, metadataDef: JsValue): Future[String] = {
    val itemKeys = (metadataXmlChild \\ "item")
      .flatMap(itemNode => itemNode.attribute("key"))
      .map(keyNode => keyNode.text)
    getAndMapValueListItemKeysToIds(metadataDef, false, itemKeys: _*)
  }

  private def handleAuthors(metadataXmlChild: Node): Future[Seq[AuthorDocumentMapping]] = {
    val swsAuthorIds = (metadataXmlChild \\ "item")
      .flatMap(itemNode => itemNode.attribute("key"))
      .map(keyNode => keyNode.text)
    metadataAuthorMapper.initCache()
    val authorDocs = swsAuthorIds
      .map(metadataAuthorMapper.mapAuthorToDocument)
      .filter(_.isDefined)
      .flatten
    Future.successful(authorDocs)
  }

  private def getAndMapValueListItemKeysToIds(metadataDef: JsValue, singleSelect: Boolean, itemKeys: String*): Future[String] = {
    val valueListPath = (metadataDef \ "settings" \ "valueListPath").as[String]
    metadataApi
      .getValueListItems(valueListPath)
      .map(allItems => mapValueListItemKeysToIds(allItems.as[JsArray], singleSelect, itemKeys))
  }

  private def mapValueListItemKeysToIds(allItems: JsArray, singleSelect: Boolean, itemKeys: Seq[String]): String = {
    val itemIds = allItems.value
      .filter(item => itemKeys.contains((item \ "key").as[String]))
      .map(item => (item \ "id").as[Int])
    if (itemIds.isEmpty) null
    else if (singleSelect) itemIds.head.toString
    else stringifyNumbers(itemIds.toSeq)
  }

  private def handleMultipleStringValue(metadataXmlChild: Node): Future[String] = {
    val stringValues = metadataXmlChild.child.map(node => node.text).toSeq
    if (stringValues.isEmpty) Future.successful(null)
    else Future.successful(stringifyStrings(stringValues))
  }

  private def getMetadataDefinitions(documentTypeKey: String, nodeType: NodeType): Future[Seq[JsValue]] =
    metadataApi
      .getMetadataDefinitions(documentTypeKey, nodeType)
      .map(jsValue => (jsValue \\ "metadataFields").toSeq.flatMap(_.as[Seq[JsValue]]))

  private def getMetadataType(metadataDefinition: JsValue): String =
    (metadataDefinition \ "baseMetadataType").as[String]

  private def isMultiSelectList(metadataDefinition: JsValue): Boolean =
    (metadataDefinition \ "settings" \ "multipleSelectList").as[Boolean]

  private def stringifyNumbers(value: Iterable[Any]): String = s"[${value.mkString(",")}]"

  private def stringifyStrings(value: Iterable[String]): String = s"[${value.map(stringify).mkString(",")}]"

  private def stringify(value: Any): String = s""""$value""""

}
