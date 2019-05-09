package domain.errors

import play.api.libs.json.{JsString, Json, Writes}

sealed abstract case class HttpError(message: String, errorType: HttpErrorType)

object HttpError {

  implicit val writes: Writes[HttpError] =
    (o: HttpError) => Json.obj("message" -> o.message, "errorType" -> Json.toJson(o.errorType))

  val unauthorised: HttpError = new HttpError("Access denied", Unauthorised) {}
}

sealed trait HttpErrorType
case object Unauthorised extends HttpErrorType

object HttpErrorType {

  implicit val writes: Writes[HttpErrorType] = {
    case Unauthorised => JsString("Unauthorised")
  }
}