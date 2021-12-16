package dto

case class UploadFileDto(
    format: String,
    paths: Option[String],
    version: Option[String],
    swsXhtml: Option[String],
    swsHtml: Option[String],
    swsJson: Option[String],
    swsXml: Option[String]
)
