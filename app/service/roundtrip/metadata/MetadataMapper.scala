package service.roundtrip.metadata

import components.publishone.MetadataApi
import dto.RoundTripDto
import play.api.Logger
import util.NodeTypes
import play.api.libs.json._

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
class MetadataMapper @Inject()(metadataApi: MetadataApi) {

  private lazy val log = Logger(getClass)

  def mapXmlMetadata(roundTripDto: RoundTripDto, metadataXml: Array[Byte]): Future[Map[String, String]] = {
    log.info(s"${roundTripDto.toString} Map XML metadata to Json started")
    for {
      metadataDefs <- getMetadataDefinitions(roundTripDto.docType)
      metadataJson <- mapXmlToJsonMetadata(metadataXml, metadataDefs)
      _ <- Future.successful(log.info(s"${roundTripDto.toString} XML metadata mapped to Json"))
    } yield metadataJson
  }

  private def mapXmlToJsonMetadata(metadataXml: Array[Byte], metadataDefs: Seq[JsValue]): Future[Map[String, String]] =
    getJsonMetadataValues(metadataXml, metadataDefs)
      .map(filterNonEmpty)
      .map(_.toMap)

  private def getJsonMetadataValues(metadataXml: Array[Byte], metadataDefs: Seq[JsValue]): Future[Seq[(String, String)]] = {
    val metadataXmlChildren = xml.XML.load(new ByteArrayInputStream(metadataXml)).child
    val getJsonMetadataValueFutures = metadataXmlChildren.map(getJsonMetadataValue(metadataDefs, _))
    Future.sequence(getJsonMetadataValueFutures)
  }

  private def getJsonMetadataValue(metadataDefs: Seq[JsValue], metadataXmlChild: Node): Future[(String, String)] = {
    val metadataName = metadataXmlChild.label
    val metadataDef = getMetadataDefinition(metadataDefs, metadataName)
    val getMetadataValue: Future[String] = getMetadataType(metadataDef) match {
      case "selectList" if isMultiSelectList(metadataDef) => handleMultiSelectList(metadataXmlChild, metadataDef)
      case "selectList"                                   => handleSingleSelectList(metadataXmlChild, metadataDef)
      case "multipleStringValue"                          => handleMultipleStringValue(metadataXmlChild)
      case _                                              => Future.successful(metadataXmlChild.text)
    }
    getMetadataValue.map((metadataName, _))
  }

  private def filterNonEmpty(jsonMetadataValues: Seq[(String, String)]): Seq[(String, String)] =
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

  private def getMetadataDefinitions(documentTypeKey: String): Future[Seq[JsValue]] =
    metadataApi
      .getMetadataDefinitions(documentTypeKey, NodeTypes.document)
      .map(jsValue => (jsValue \\ "metadataFields").toSeq.flatMap(_.as[Seq[JsValue]]))

  private def getMetadataDefinition(metadataDefinitions: Seq[JsValue], name: String): JsValue =
    metadataDefinitions
      .filter(metaDefinition => (metaDefinition \ "name").as[String] == name)
      .head

  private def getMetadataType(metadataDefinition: JsValue): String =
    (metadataDefinition \ "baseMetadataType").as[String]

  private def isMultiSelectList(metadataDefinition: JsValue): Boolean =
    (metadataDefinition \ "settings" \ "multipleSelectList").as[Boolean]

  private def stringifyNumbers(value: Iterable[Any]): String = s"[${value.mkString(",")}]"

  private def stringifyStrings(value: Iterable[String]): String = s"[${value.map(stringify).mkString(",")}]"

  private def stringify(value: Any): String = s""""$value""""

}
