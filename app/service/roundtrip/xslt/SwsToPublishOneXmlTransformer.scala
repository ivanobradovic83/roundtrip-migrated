package service.roundtrip.xslt

import play.api.Logger
import service.roundtrip.model.RoundTripDocument
import util.PublishOneConstants.{xsltLocation, xsltRootLocation}
import util.XmlTransformer

import java.io.{ByteArrayInputStream, File}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SwsToPublishOneXmlTransformer {

  private lazy val log = Logger(getClass)

  def transform(roundTripDoc: RoundTripDocument, xhtml: Array[Byte], metaXml: Array[Byte]): Future[(Array[Byte], Array[Byte])] = Future {
    log.info(s"$roundTripDoc applying XSL transformations")

    val transformationInputXml =
      <document> {xml.XML.load(new ByteArrayInputStream(xhtml))} {xml.XML.load(new ByteArrayInputStream(metaXml))} </document>
    val transformedXml = applyAllTransformations(roundTripDoc, transformationInputXml.toString().getBytes)
    val transformedDocumentXml = (xml.XML.load(new ByteArrayInputStream(transformedXml)) \ "document").toString.getBytes
    val transformedMetaXml = (xml.XML.load(new ByteArrayInputStream(transformedXml)) \ "meta").toString.getBytes

    log.debug(s"$roundTripDoc XSL transformations are applied")
    (transformedDocumentXml, transformedMetaXml)
  }

  private def applyAllTransformations(roundTripDoc: RoundTripDocument, transformationInputXml: Array[Byte]): Array[Byte] = {
    val xsltFolderPath = s"$xsltRootLocation/${roundTripDoc.docType}"
    var currentTransformation = transformationInputXml

    for (xsltName <- getXslTransformationNames(xsltFolderPath)) {
      log.debug(s"$roundTripDoc applying XSLT $xsltFolderPath/$xsltName")
      currentTransformation = new XmlTransformer(s"$xsltLocation/${roundTripDoc.docType}/$xsltName").transform(currentTransformation)
    }
    currentTransformation
  }

  private def getXslTransformationNames(xsltFolderPath: String): Array[String] =
    new File(xsltFolderPath)
      .listFiles()
      .map(_.getName)
      .sorted

}
