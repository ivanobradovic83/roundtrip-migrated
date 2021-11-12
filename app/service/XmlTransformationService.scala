package service

import dto.RoundTripDto
import play.api.Logger
import util.XmlTransformer

import java.io.ByteArrayInputStream
import scala.concurrent.Future

class XmlTransformationService {

  private lazy val log = Logger(getClass)
  private lazy val voorwasTransformer = new XmlTransformer("/xslt/1-voorwas.xsl")
  private lazy val generiekTransformer = new XmlTransformer("/xslt/2-generiek.xsl")
  private lazy val specifiekTransformer = new XmlTransformer("/xslt/3-specifiek.xsl")
  private lazy val metadataTransformer = new XmlTransformer("/xslt/4-metadata.xsl")

  def transform(roundTripDto: RoundTripDto, xhtml: Array[Byte], metaXml: Array[Byte]): Future[(Array[Byte], Array[Byte])] = {
    log.info(s"${roundTripDto.toString} XSL transformation started")

    val transformationInputXml =
      <document> { xml.XML.load(new ByteArrayInputStream(xhtml)) } {xml.XML.load(new ByteArrayInputStream(metaXml))} </document>
    val transformedXml = applyAllTransformations(transformationInputXml.toString().getBytes)
    val transformedDocumentXml = (xml.XML.load(new ByteArrayInputStream(transformedXml)) \ "document").toString.getBytes
    val transformedMetaXml = (xml.XML.load(new ByteArrayInputStream(transformedXml)) \ "meta").toString.getBytes
    // tmp
    xml.XML.save("./transformed-all.xhtml", xml.XML.load(new ByteArrayInputStream(transformedXml)))
    xml.XML.save("./transformed-doc.xhtml", xml.XML.load(new ByteArrayInputStream(transformedDocumentXml)))
    xml.XML.save("./transformed-meta.xhtml", xml.XML.load(new ByteArrayInputStream(transformedMetaXml)))

    log.info(s"${roundTripDto.toString} XSL transformation ended")
    Future.successful((transformedDocumentXml, transformedMetaXml))
  }

  private def applyAllTransformations(transformationInputXml: Array[Byte]) = {
    val voorwas = voorwasTransformer.transform(transformationInputXml)
    val generiek = generiekTransformer.transform(voorwas)
    val specifiek = specifiekTransformer.transform(generiek)
    metadataTransformer.transform(specifiek)
  }
}
