package validators

import cats.data.ValidatedNel
import cats.implicits._
import com.google.inject.{Inject, Singleton}
import domain.AccountCreate
import domain.errors.AccountError
import play.api.i18n.Messages

@Singleton
class AccountCreateValidator @Inject()() {

  def validate(a: AccountCreate)(implicit messages: Messages): ValidatedNel[AccountError, AccountCreate] =
    emailValid(a.email).combine(passwordValid(a.password)).map(_ => a)

  private def emailValid(email: String)(implicit messages: Messages): ValidatedNel[AccountError, String] =
    email match {
      case s if s.matches("^[^@]+@[^@]+\\.[^@]+$") =>
        email.validNel[AccountError]
      case ""                      => AccountError.Email.missing.invalidNel[String]
      case _                       => AccountError.Email.formatting.invalidNel[String]
    }

  private def passwordValid(password: String)(implicit messages: Messages): ValidatedNel[AccountError, String] =
    password match {
      case "" => AccountError.Password.missing.invalidNel[String]
      case _  => password.validNel[AccountError]
    }
}