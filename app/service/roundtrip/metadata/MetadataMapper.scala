package service.roundtrip.metadata

import components.publishone.MetadataApi
import play.api.libs.json._

import java.io.ByteArrayInputStream
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.Node

/**
  * This class maps given metadata in XML format to Json format by executing next steps:
  * <pre>
  * - parse given metadataXml
  * - traverse through all Xml metadata
  * -  maps values from XML to Json value
  * - returns list of metadata in Json format which are ready to be used with PublishOne API
  * </pre>
  *
  * @param metadataApi PublishOne Metadata API
  */
class MetadataMapper @Inject()(metadataApi: MetadataApi) {

  def mapXmlToJsonMetadata(metadataXml: Array[Byte]): Future[JsValue] =
    for {
      metadataDefs <- getMetadataDefinitions
      metadataJson <- mapXmlToJsonMetadata(metadataXml, metadataDefs)
    } yield metadataJson

  private def mapXmlToJsonMetadata(metadataXml: Array[Byte], metadataDefs: Seq[JsValue]): Future[JsValue] =
    getJsonMetadataValues(metadataXml, metadataDefs)
      .map(convertToSeqOfJsonMetadata)
      .map(jsonMetadataSeq => Json.toJson(jsonMetadataSeq))

  private def getJsonMetadataValues(metadataXml: Array[Byte], metadataDefs: Seq[JsValue]): Future[Seq[(String, JsValue)]] = {
    val metadataXmlChildren = xml.XML.load(new ByteArrayInputStream(metadataXml)).child
    val getJsonMetadataValueFutures = metadataXmlChildren.map(getJsonMetadataValue(metadataDefs, _))
    Future.sequence(getJsonMetadataValueFutures)
  }

  private def getJsonMetadataValue(metadataDefs: Seq[JsValue], metadataXmlChild: Node): Future[(String, JsValue)] = {
    val metadataName = metadataXmlChild.label
    val metadataDef = getMetadataDefinition(metadataDefs, metadataName)
    val getMetadataValue: Future[JsValue] = getMetadataType(metadataDef) match {
      case "selectList" if isMultiSelectList(metadataDef) => handleMultiSelectList(metadataXmlChild, metadataDef)
      case "selectList"                                   => handleSingleSelectList(metadataXmlChild, metadataDef)
      case "multipleStringValue"                          => handleMultipleStringValue(metadataXmlChild)
      case _                                              => Future.successful(JsString(metadataXmlChild.text))
    }
    getMetadataValue.map(value => (metadataName, value))
  }

  private def convertToSeqOfJsonMetadata(jsonMetadataValues: Seq[(String, JsValue)]): Seq[JsObject] = {
    jsonMetadataValues
      .filter(_._2 != JsNull)
      .map(createJsonMetadata)
  }

  private def createJsonMetadata(metadata: (String, JsValue)): JsObject =
    Json.obj(
      "name" -> metadata._1,
      "value" -> metadata._2,
      "updateOperation" -> "replace"
    )

  private def handleSingleSelectList(metadataXmlChild: Node, metadataDef: JsValue): Future[JsValue] = {
    val itemKey = metadataXmlChild.attribute("key").map(keyNode => keyNode.text).head
    getAndMapValueListItemKeysToIds(metadataDef, true, itemKey)
  }

  private def handleMultiSelectList(metadataXmlChild: Node, metadataDef: JsValue): Future[JsValue] = {
    val itemKeys = (metadataXmlChild \\ "item")
      .flatMap(itemNode => itemNode.attribute("key"))
      .map(keyNode => keyNode.text)
    getAndMapValueListItemKeysToIds(metadataDef, false, itemKeys: _*)
  }

  private def getAndMapValueListItemKeysToIds(metadataDef: JsValue, singleSelect: Boolean, itemKeys: String*): Future[JsValue] = {
    val valueListPath = (metadataDef \ "settings" \ "valueListPath").as[String]
    metadataApi
      .getValueListItems(valueListPath)
      .map(allItems => mapValueListItemKeysToIds(allItems.as[JsArray], singleSelect, itemKeys))
  }

  private def mapValueListItemKeysToIds(allItems: JsArray, singleSelect: Boolean, itemKeys: Seq[String]): JsValue = {
    val itemIds = allItems.value
      .filter(item => itemKeys.contains((item \ "key").as[String]))
      .map(item => (item \ "id").as[Int])
    if (itemIds.isEmpty) JsNull
    else if (singleSelect) stringifyJson(Json.toJson(itemIds.head))
    else stringifyJson(Json.toJson(itemIds))
  }

  private def handleMultipleStringValue(metadataXmlChild: Node): Future[JsValue] = {
    val stringValues = metadataXmlChild.child.map(node => node.text).toSeq
    if (stringValues.isEmpty) Future.successful(JsNull)
    else Future.successful(stringifyJson(Json.toJson(stringValues)))
  }

  private def getMetadataDefinitions: Future[Seq[JsValue]] =
    metadataApi
      .getDocumentMetadataDefinitions()
      .map(jsValue => (jsValue \\ "metadataFields").toSeq.flatMap(_.as[Seq[JsValue]]))

  private def getMetadataDefinition(metadataDefinitions: Seq[JsValue], name: String): JsValue =
    metadataDefinitions
      .filter(metaDefinition => (metaDefinition \ "name").as[String] == name)
      .head

  private def getMetadataType(metadataDefinition: JsValue): String =
    (metadataDefinition \ "baseMetadataType").as[String]

  private def isMultiSelectList(metadataDefinition: JsValue): Boolean =
    (metadataDefinition \ "settings" \ "multipleSelectList").as[Boolean]

  private def stringifyJson(json: JsValue): JsString = JsString(Json.stringify(json))

}
