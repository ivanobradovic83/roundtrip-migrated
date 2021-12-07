package dto

import util.RoundTripActions.RoundTripAction

case class RoundTripDto(id: String, swsQuery: String, docType: String, destination: String, action: RoundTripAction)
