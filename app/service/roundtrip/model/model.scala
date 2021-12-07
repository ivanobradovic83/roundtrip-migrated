package service.roundtrip.model

case class AuthorDocumentMapping(id: Int, title: String, authorItemId: String)

case class RoundTripDocument(roundTripId: String, docKey: String, docType: String, destination: String)

case class ImportedDocument(folderId: Int, folderName: String, documentIds: Seq[Int])
