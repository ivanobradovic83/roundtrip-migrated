package service

import util.XmlTransformer

class XmlTransformationService {

  def transformDocument(transformationPath: String, xhtml: Array[Byte]): Array[Byte] = {
    val xmlTransformer = new XmlTransformer(transformationPath)
    xmlTransformer.transform(xhtml)
  }

}
