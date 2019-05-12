package domain.errors

import play.api.i18n.Messages
import play.api.libs.json.{JsString, Json, Writes}

sealed abstract case class DriveError(msg: String, errorType: DriveErrorType)

object DriveError {

  implicit val writes: Writes[DriveError] = (o: DriveError) =>
    Json.obj("message" -> o.msg, "errorType" -> Json.toJson(o.errorType))

  object Address {

    def firstLineRequired(implicit messages: Messages): DriveError =
      new DriveError(messages("error.drive.address.firstLine.required"), AddressFirstLineRequired) {}

    def postcodeRequired(implicit messages: Messages): DriveError =
      new DriveError(messages("error.drive.address.postcode.required"), AddressPostcodeRequired) {}
  }

  object Availability {

    def toBeforeFrom(implicit messages: Messages): DriveError =
      new DriveError(messages("error.drive.availability.toBeforeFrom"), AvailabilityToBeforeFrom) {}
  }

  def unableToAssignPID(implicit messages: Messages): DriveError =
    new DriveError(messages("error.drive.pid.assign"), UnableToAssignPID) {}
}

sealed trait DriveErrorType
case object AddressFirstLineRequired extends DriveErrorType
case object AddressPostcodeRequired extends DriveErrorType
case object AvailabilityToBeforeFrom extends DriveErrorType
case object UnableToAssignPID extends DriveErrorType

object DriveErrorType {

  private val addressFirstLineRequired = "addressFirstLineRequired"
  private val addressPostcodeRequired  = "addressPostcodeRequired"
  private val availabilityToBeforeFrom = "availabilityToBeforeFrom"
  private val unableToAssignPID = "unableToAssignPID"

  implicit val writes: Writes[DriveErrorType] = {
    case AddressFirstLineRequired => JsString(addressFirstLineRequired)
    case AddressPostcodeRequired => JsString(addressPostcodeRequired)
    case AvailabilityToBeforeFrom => JsString(availabilityToBeforeFrom)
    case UnableToAssignPID => JsString(unableToAssignPID)
  }
}