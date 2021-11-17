package service

import dto.RoundTripDto
import play.api.Logger
import util.{PublishOneConstants, XmlTransformer}

import java.io.ByteArrayInputStream
import java.io.File
import scala.concurrent.Future


class XmlTransformationService {

  private lazy val log = Logger(getClass)

  def transform(roundTripDto: RoundTripDto, xhtml: Array[Byte], metaXml: Array[Byte], documentTypeId: String): Future[(Array[Byte], Array[Byte])] = {
    log.info(s"${roundTripDto.toString} XSL transformation started")

    val transformationInputXml =
      <document> { xml.XML.load(new ByteArrayInputStream(xhtml)) } {xml.XML.load(new ByteArrayInputStream(metaXml))} </document>
    val transformedXml = applyAllTransformations(transformationInputXml.toString().getBytes, documentTypeId)
    val transformedDocumentXml = (xml.XML.load(new ByteArrayInputStream(transformedXml)) \ "document").toString.getBytes
    val transformedMetaXml = (xml.XML.load(new ByteArrayInputStream(transformedXml)) \ "meta").toString.getBytes
    // tmp
    xml.XML.save("./transformed-all.xhtml", xml.XML.load(new ByteArrayInputStream(transformedXml)))
    xml.XML.save("./transformed-doc.xhtml", xml.XML.load(new ByteArrayInputStream(transformedDocumentXml)))
    xml.XML.save("./transformed-meta.xhtml", xml.XML.load(new ByteArrayInputStream(transformedMetaXml)))

    log.info(s"${roundTripDto.toString} XSL transformation ended")
    Future.successful((transformedDocumentXml, transformedMetaXml))
  }

  private def applyAllTransformations(transformationInputXml: Array[Byte], documentType: String): Array[Byte] = {
    val xsltPath = getTransformationsPathByDocType(documentType)
    var currentTransformation = transformationInputXml;

    for(transformationName <- getTransformationNames(xsltPath)) {
      currentTransformation = new XmlTransformer(s"$xsltPath/$transformationName").transform(currentTransformation)
    }
    currentTransformation
  }

  private def getTransformationsPathByDocType(documentTypeId: String): String ={
    documentTypeId match {
      case "commentaar" => PublishOneConstants.documentTypeCommentaarXsltLocation
    }
  }

  private def getTransformationNames(xslLocation: String): Array[String] = {
    val files: Array[File] = new File(s"./conf/$xslLocation").listFiles()
    val list = for(file <- files) yield file.getName
    list.sortWith(_ < _)
  }

}
