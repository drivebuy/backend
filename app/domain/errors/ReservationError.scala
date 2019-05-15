package domain.errors

import play.api.i18n.Messages
import play.api.libs.json.{JsString, Json, Writes}

sealed abstract case class ReservationError(msg: String, errorType: ReservationErrorType)

object ReservationError {

  implicit val writes: Writes[ReservationError] = (o: ReservationError) =>
    Json.obj("message" -> o.msg, "errorType" -> Json.toJson(o.errorType))

  def invalidPID(implicit messages: Messages): ReservationError =
    new ReservationError(messages("error.reservation.invalidPID"), InvalidPID) {}

  def negativeDuration(implicit messages: Messages): ReservationError =
    new ReservationError(messages("error.reservation.negativeDuration"), NegativeDuration) {}

  def invalidPeriod(implicit messages: Messages): ReservationError =
    new ReservationError(messages("error.reservation.invalidPeriod"), InvalidPeriod) {}

}

sealed trait ReservationErrorType
case object InvalidPID extends ReservationErrorType
case object NegativeDuration extends ReservationErrorType
case object InvalidPeriod extends ReservationErrorType

object ReservationErrorType {

  private val invalidPID       = "invalidPID"
  private val negativeDuration = "negativeDuration"
  private val invalidPeriod    = "invalidPeriod"

  implicit val writes: Writes[ReservationErrorType] = {
    case InvalidPID       => JsString(invalidPID)
    case NegativeDuration => JsString(negativeDuration)
    case InvalidPeriod    => JsString(invalidPeriod)
  }
}