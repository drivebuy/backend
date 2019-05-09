package domain

import java.util.UUID

import cats.implicits._
import play.api.i18n.Messages
import play.api.libs.json._
import utils.{Hashing, Salting}


case class Account(id: UUID, email: String, password: Password)

object Account {

  implicit val formats = Json.format[Account]
}

sealed abstract case class Password(value: String, salt: String)

object Password {

  implicit val writes: Format[Password] = new Format[Password] {
    override def writes(o: Password): JsValue =
      Json.obj("value" -> Json.toJson(o.value), "salt" -> Json.toJson(o.salt))

    override def reads(json: JsValue): JsResult[Password] = {
      val value = (json \ "value").asOpt[String]
      val salt  = (json \ "salt").asOpt[String]

      (value, salt).mapN { case (v, s) => JsSuccess(new Password(v, s) {}) }
        .getOrElse(JsError(s"Failed json read: $json"))
    }
  }


  def apply(password: String, salt: String): Password =
    new Password(Hashing.hash(password, salt), salt) {}
}

case class AccountCreate(email: String, password: String) {

  lazy val asAccount: Account =
    Account(
      UUID.randomUUID(),
      email,
      Password(password, Salting.salt))
}

object AccountCreate {

  implicit val formats = Json.format[AccountCreate]
}

case class AccountLogin(email: String, password: String)

object AccountLogin {

  implicit val reads = Json.reads[AccountLogin]
}

case class AccountSession(sessionId: UUID)

object AccountSession {

  implicit val writes = Json.writes[AccountSession]
}

sealed abstract case class AccountCreated(message: String)

object AccountCreated {

  def success(implicit messages: Messages): AccountCreated = new AccountCreated(messages("account.created")) {}

  implicit val writes: Writes[AccountCreated] = (o: AccountCreated) => Json.obj("message" -> o.message)
}