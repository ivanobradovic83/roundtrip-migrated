package service

import components.publishone.MetadataApi
import play.api.libs.json._

import java.io.ByteArrayInputStream
import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.Node

class MetadataMappingService @Inject()(metadataApi: MetadataApi) {

  def mapXmlToJsonMetadata(metaXml: Array[Byte]): Future[JsValue] =
    for {
      metadataDefinitions <- getMetadataDefinitions
      jsonMetadata <- mapXmlToJsonMetadata(metaXml, metadataDefinitions)
    } yield jsonMetadata

  private def mapXmlToJsonMetadata(metaXml: Array[Byte], metadataDefinitions: Seq[JsValue]): Future[JsValue] = {
    getJsonMetadataValues(metaXml, metadataDefinitions)
      .map(convertToJsonMetadataSeq)
      .map(jsonMetadataSeq => Json.toJson(jsonMetadataSeq))
  }

  private def getJsonMetadataValues(metaXml: Array[Byte], metadataDefinitions: Seq[JsValue]): Future[Seq[(String, JsValue)]] = {
    val futures = ListBuffer[Future[(String, JsValue)]]()
    val metaXmlChildren = xml.XML.load(new ByteArrayInputStream(metaXml)).child
    for (metaXmlChild <- metaXmlChildren) {
      val metaName = metaXmlChild.label
      val metaDefinition = getMetadataDefinition(metadataDefinitions, metaName)
      val metaValueFuture: Future[JsValue] = getMetadataType(metaDefinition) match {
        case "selectList" if isMultiSelectList(metaDefinition) => handleMultiSelectList(metaXmlChild, metaDefinition)
        case "selectList"                                      => handleSingleSelectList(metaXmlChild, metaDefinition)
        case "multipleStringValue"                             => handleMultipleStringValue(metaXmlChild)
        case _                                                 => Future.successful(JsString(metaXmlChild.text))
      }
      futures += metaValueFuture.map(value => (metaName, value))
    }
    Future.sequence(futures.toSeq)
  }

  private def convertToJsonMetadataSeq(jsonMetadataValues: Seq[(String, JsValue)]): Seq[JsObject] = {
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

  private def handleSingleSelectList(metaXmlChild: Node, metadataDefinition: JsValue): Future[JsValue] = {
    val itemKey = metaXmlChild.attribute("key").map(keyNode => keyNode.text).head
    getAndMapValueListItemKeysToIds(metadataDefinition, true, itemKey)
  }

  private def handleMultiSelectList(metaXmlChild: Node, metadataDefinition: JsValue): Future[JsValue] = {
    val itemKeys = (metaXmlChild \\ "item")
      .flatMap(itemNode => itemNode.attribute("key"))
      .map(keyNode => keyNode.text)
    getAndMapValueListItemKeysToIds(metadataDefinition, false, itemKeys: _*)
  }

  private def getAndMapValueListItemKeysToIds(metadataDefinition: JsValue, singleSelect: Boolean, itemKeys: String*): Future[JsValue] = {
    val valueListPath = (metadataDefinition \ "settings" \ "valueListPath").as[String]
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

  private def handleMultipleStringValue(metaXmlNode: Node): Future[JsValue] = {
    val stringValues = metaXmlNode.child.map(node => node.text).toSeq
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
