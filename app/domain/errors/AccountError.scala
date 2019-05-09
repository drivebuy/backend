package domain.errors

import play.api.i18n.Messages
import play.api.libs.json.{JsString, Json, Writes}

sealed abstract case class AccountError(msg: String, errorType: AccountErrorType)

object AccountError {

  implicit val format: Writes[AccountError] = (o: AccountError) =>
    Json.obj("message" -> o.msg, "errorType" -> Json.toJson(o.errorType))

  object Email {

    def missing(implicit messages: Messages): AccountError =
      new AccountError(messages("error.email.required"), MissingEmail) {}

    def duplicate(implicit messages: Messages): AccountError =
      new AccountError(messages("error.email.duplicate"), DuplicateEmail) {}

    def formatting(implicit messages: Messages): AccountError =
      new AccountError(messages("error.email.format"), FormattingEmail) {}
  }

  object Password {

    def missing(implicit messages: Messages): AccountError =
      new AccountError(messages("error.password.missing"), MissingPassword) {}
  }

  def invalidCreds(implicit messages: Messages): AccountError =
    new AccountError(messages("error.creds.invalid"), InvalidCreds) {}
}

sealed trait AccountErrorType
case object MissingEmail extends AccountErrorType
case object DuplicateEmail extends AccountErrorType
case object FormattingEmail extends AccountErrorType
case object MissingPassword extends AccountErrorType
case object InvalidCreds extends AccountErrorType

object AccountErrorType {

  implicit val writes: Writes[AccountErrorType] = {
    case MissingEmail    => JsString("missingEmail")
    case DuplicateEmail  => JsString("duplicateEmail")
    case FormattingEmail => JsString("formattingEmail")
    case MissingPassword => JsString("missingPassword")
    case InvalidCreds    => JsString("invalidCreds")
  }
}