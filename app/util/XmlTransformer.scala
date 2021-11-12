package util

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import javax.xml.transform.stream.StreamSource
import net.sf.saxon.s9api.{Processor, QName, Serializer, XdmAtomicValue, XsltExecutable, XsltTransformer}

import scala.concurrent.blocking

class XmlTransformer(xsltPath: String) {

  lazy val xsltExec: XsltExecutable =
    new Processor(true)
      .newXsltCompiler()
      .compile(new StreamSource(getClass.getResource(xsltPath).toString))

  def transform(inputXml: Array[Byte], transformationParameters: Map[String, String] = Map.empty): Array[Byte] =
    blocking {
      val transformer: XsltTransformer = xsltExec.load
      transformationParameters.foreach {
        case (key, value) =>
          transformer.setParameter(new QName(key), new XdmAtomicValue(value))
      }

      val result = new ByteArrayOutputStream
      transformer.setSource(new StreamSource(new ByteArrayInputStream(inputXml)))
      val out = new Serializer(result)
      out.setOutputProperty(Serializer.Property.INDENT, "no")
      out.setOutputProperty(Serializer.Property.ENCODING, "UTF-8")
      transformer.setDestination(out)

      transformer.transform()
      transformer.close()

      result.toByteArray
    }

}
