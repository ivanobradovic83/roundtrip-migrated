package service.roundtrip.xslt

import helpers.ScalaSpec
import service.roundtrip.model.RoundTripDocument

import java.nio.file.{Files, Paths}
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.xml.{Elem, XML}

class SwsToPublishOneXmlTransformerSpec extends ScalaSpec {

  lazy val roundTripDoc: RoundTripDocument = RoundTripDocument("rt_id", "doc_key", "commentaar", "destination")
  lazy val xhtml: Array[Byte] = Files.readAllBytes(Paths.get("test/resources/xslt/c-AAN-W43570-2.62.xhtml"))
  lazy val metaXml: Array[Byte] = Files.readAllBytes(Paths.get("test/resources/xslt/c-AAN-W43570-2.62.xml"))
  lazy val expectedDocXml: Elem = XML.loadFile("test/resources/xslt/transformed-doc.xml")
  lazy val expectedMetaXml: Elem = XML.loadFile("test/resources/xslt/transformed-meta.xml")

  val cut: SwsToPublishOneXmlTransformer = new SwsToPublishOneXmlTransformer()

  "when CWC document transformed to PublishOne format then verify result is as expected" in {
    val result: (Array[Byte], Array[Byte]) = Await.result(cut.transform(roundTripDoc, xhtml, metaXml), 10.seconds)

    val p1DocXmlString = new String(result._1)
    val p1DocXml = XML.loadString(p1DocXmlString)
    p1DocXml should be(expectedDocXml)

    val p1MetaXmlString = new String(result._2)
    val p1MetaXml = XML.loadString(p1MetaXmlString)
    p1MetaXml should be(expectedMetaXml)
  }

}
