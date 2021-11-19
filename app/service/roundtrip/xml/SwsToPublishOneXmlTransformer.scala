package service.roundtrip.xml

import dto.RoundTripDto
import play.api.Logger
import util.PublishOneConstants.{xsltLocation, xsltRootLocation}
import util.XmlTransformer

import java.io.{ByteArrayInputStream, File}
import scala.concurrent.Future

class SwsToPublishOneXmlTransformer {

  private lazy val log = Logger(getClass)

  def transform(roundTripDto: RoundTripDto, xhtml: Array[Byte], metaXml: Array[Byte]): Future[(Array[Byte], Array[Byte])] = {
    log.info(s"${roundTripDto.toString} XSL transformation started")

    val transformationInputXml =
      <document> { xml.XML.load(new ByteArrayInputStream(xhtml)) } {xml.XML.load(new ByteArrayInputStream(metaXml))} </document>
    val transformedXml = applyAllTransformations(roundTripDto, transformationInputXml.toString().getBytes)
    val transformedDocumentXml = (xml.XML.load(new ByteArrayInputStream(transformedXml)) \ "document").toString.getBytes
    val transformedMetaXml = (xml.XML.load(new ByteArrayInputStream(transformedXml)) \ "meta").toString.getBytes
    // tmp
    xml.XML.save("./transformed-all.xhtml", xml.XML.load(new ByteArrayInputStream(transformedXml)))
    xml.XML.save("./transformed-doc.xhtml", xml.XML.load(new ByteArrayInputStream(transformedDocumentXml)))
    xml.XML.save("./transformed-meta.xhtml", xml.XML.load(new ByteArrayInputStream(transformedMetaXml)))

    log.info(s"${roundTripDto.toString} XSL transformation ended")
    Future.successful((transformedDocumentXml, transformedMetaXml))
  }

  private def applyAllTransformations(roundTripDto: RoundTripDto, transformationInputXml: Array[Byte]): Array[Byte] = {
    val xsltFolderPath = s"$xsltRootLocation/${roundTripDto.docType}"
    var currentTransformation = transformationInputXml

    for (xsltName <- getXslTransformationNames(xsltFolderPath)) {
      log.debug(s"${roundTripDto.toString} applying XSLT  $xsltFolderPath/$xsltName")
      currentTransformation = new XmlTransformer(s"$xsltLocation/${roundTripDto.docType}/$xsltName").transform(currentTransformation)
    }
    currentTransformation
  }

  private def getXslTransformationNames(xsltFolderPath: String): Array[String] =
    new File(xsltFolderPath)
      .listFiles()
      .map(_.getName)
      .sorted

}
