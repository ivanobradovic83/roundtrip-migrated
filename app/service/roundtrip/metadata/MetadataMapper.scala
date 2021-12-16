package service.roundtrip.metadata

import components.publishone.MetadataApi
import play.api.Logger
import play.api.libs.json.JsValue
import service.common.cache.ValueListCache
import service.roundtrip.model.{AuthorDocumentMapping, RoundTripDocument}
import util.NodeTypes.NodeType
import util.PublishOneConstants._
import util.PublishOneUtils._
import util.StringUtils._

import java.io.ByteArrayInputStream
import javax.inject.{Inject, Singleton}
import scala.collection.concurrent.TrieMap
import scala.collection.immutable.Iterable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.Node

/** This class maps given metadata in XML format to string values which will be used as Json values by executing next steps: <pre>
  *   - parse given metadataXml
  *   - traverse through all Xml metadata
  *   - maps values from XML to Json value
  *   - returns Map of Json metadata values which are ready to be used with PublishOne API </pre>
  *
  * @param metadataApi
  *   PublishOne Metadata API
  */
@Singleton
class MetadataMapper @Inject()(metadataApi: MetadataApi, metadataAuthorMapper: AuthorDocumentMapper, valueListCache: ValueListCache) {

  private lazy val log = Logger(getClass)
  protected[metadata] lazy val metadataDefCache: TrieMap[String, Map[String, JsValue]] = new TrieMap[String, Map[String, JsValue]]()

  def initCache(types: Seq[(String, NodeType)]): Future[Any] =
    Future.sequence(types.map {
      case (docType, nodeType) =>
        loadMetadataDefinitionToCache(docType, nodeType)
    })

  def cleanCache(): Unit = metadataDefCache.clear()

  def mapXmlMetadata(roundTripDoc: RoundTripDocument, metadataXmlContent: Array[Byte], nodeType: NodeType): Future[Map[String, AnyRef]] = Future {
    log.info(s"$roundTripDoc Mapping $nodeType XML metadata to Json")
    val metadataCacheKey = buildMetadataCacheKey(roundTripDoc.docType, nodeType)
    metadataDefCache.get(metadataCacheKey) match {
      case Some(metadataDef) =>
        val jsonMetadata = mapXmlToJsonMetadata(metadataXmlContent, metadataDef)
        log.debug(s"$roundTripDoc $nodeType XML metadata to Json mapped")
        jsonMetadata
      case None =>
        log.warn(s"There is no cached metadata definitions for '$metadataCacheKey'")
        Map.empty[String, AnyRef]
    }
  }

  private def loadMetadataDefinitionToCache(docType: String, nodeType: NodeType): Future[Unit] =
    metadataApi
      .getMetadataDefinitions(docType, nodeType)
      .map(getMetadataFields)
      .map(cacheMetadataFields(buildMetadataCacheKey(docType, nodeType), _))

  private def cacheMetadataFields(metadataKey: String, metadataFields: Seq[JsValue]): Unit = {
    val metadataFieldsMap = metadataFields
      .map(toMetadataNameAndData)
      .filter(_._1 != "")
      .toMap
    metadataDefCache += (metadataKey -> metadataFieldsMap)
  }

  private def buildMetadataCacheKey(docType: String, nodeType: NodeType) = s"$docType-$nodeType"

  private def toMetadataNameAndData(metadataField: JsValue): (String, JsValue) = {
    val metadataName = (metadataField \ "name").asOpt[String].getOrElse("")
    if (isEmpty(metadataName)) log.warn(s"Missing metadata field name $metadataField")
    (metadataName, metadataField)
  }

  private def mapXmlToJsonMetadata(metadataXmlContent: Array[Byte], metadataDef: Map[String, JsValue]): Map[String, AnyRef] = {
    val metadataXmlChildren = xml.XML.load(new ByteArrayInputStream(metadataXmlContent)).child
    metadataDef
      .map {
        case (metadataDefName, metadataDef) =>
          val metadataJsonValue = getJsonMetadataValue(metadataDefName, metadataDef, metadataXmlChildren)
          (metadataDefName, metadataJsonValue)
      }
      .filter(_._2 != null)
  }

  private def getJsonMetadataValue(metadataName: String, metadataDef: JsValue, metadataXmlChildren: Seq[Node]): AnyRef =
    metadataXmlChildren.find(_.label == metadataName) match {
      case Some(metadataValue) => getJsonMetadataValue(metadataName, metadataDef, metadataValue)
      case None                => null
    }

  private def getJsonMetadataValue(metadataName: String, metadataDef: JsValue, metadataXmlChild: Node): AnyRef =
    getMetadataType(metadataDef) match {
      case "selectList" if metadataName == listItemsAuthor => mapAuthorDocuments(metadataXmlChild)
      case "selectList" if isMultiSelectList(metadataDef)  => handleMultiSelectList(metadataName, metadataXmlChild)
      case "selectList"                                    => handleSingleSelectList(metadataName, metadataXmlChild)
      case "multipleStringValue"                           => handleMultipleStringValue(metadataXmlChild)
      case _                                               => metadataXmlChild.text
    }

  private def mapAuthorDocuments(metadataXmlChild: Node): Seq[AuthorDocumentMapping] = {
    val swsAuthorIds = (metadataXmlChild \\ "item")
      .flatMap(itemNode => itemNode.attribute("key"))
      .map(keyNode => keyNode.text)
    swsAuthorIds
      .map(metadataAuthorMapper.mapAuthorToDocument)
      .filter(_.isDefined)
      .flatten
  }

  private def handleMultiSelectList(metadataName: String, metadataXmlChild: Node): String = {
    val itemKeys = (metadataXmlChild \\ "item")
      .flatMap(itemNode => itemNode.attribute("key"))
      .map(keyNode => keyNode.text)
    mapValueListItemKeysToIds(metadataName, false, itemKeys: _*)
  }

  private def handleSingleSelectList(metadataName: String, metadataXmlChild: Node): String = {
    val itemKey = metadataXmlChild.attribute("key").map(keyNode => keyNode.text).head
    mapValueListItemKeysToIds(metadataName, true, itemKey)
  }

  private def mapValueListItemKeysToIds(metadataName: String, singleSelect: Boolean, itemKeys: String*): String = {
    val itemIds = itemKeys.map(valueListCache.mapValueListItemId(metadataName, _)).filter(notEmpty)
    if (itemIds.isEmpty) null
    else if (singleSelect) itemIds.head
    else stringifyNumbers(itemIds)
  }

  private def handleMultipleStringValue(metadataXmlChild: Node): String = {
    val stringValues = metadataXmlChild.child.map(node => node.text).toSeq
    if (stringValues.isEmpty) null
    else stringifyStrings(stringValues)
  }

  private def getMetadataType(metadataDefinition: JsValue): String =
    (metadataDefinition \ "baseMetadataType").asOpt[String] match {
      case Some(metadataType) => metadataType
      case None               => throw new Exception(s"Missing baseMetadataType in $metadataDefinition")
    }

  private def isMultiSelectList(metadataDefinition: JsValue): Boolean =
    (metadataDefinition \ "settings" \ "multipleSelectList").as[Boolean]

  private def stringifyNumbers(value: Iterable[Any]): String = s"[${value.mkString(",")}]"

  private def stringifyStrings(value: Iterable[String]): String = s"[${value.map(stringify).mkString(",")}]"

  private def stringify(value: Any): String = s""""$value""""

}
