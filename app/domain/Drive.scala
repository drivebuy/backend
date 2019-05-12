package domain

import java.time.LocalTime
import java.util.UUID

import play.api.libs.json._

sealed abstract case class PID(value: String)

object PID {

  def apply(pid: Int): PID =
    new PID(pid.formatted("%04d")) {}

  implicit val format: Format[PID] = new Format[PID] {
    override def reads(json: JsValue): JsResult[PID] = json match {
      case JsString(value) => JsSuccess(new PID(value) {})
      case _               => JsError(s"Failed to parse PID from $json")
    }

    override def writes(o: PID): JsValue = Json.toJson(o.value)
  }
}

case class Address(firstLine: String, secondLine: String, county: String, postcode: String)

object Address {

  implicit val format = Json.format[Address]
}

case class Availability(dayOfWeek: DayOfWeek, from: LocalTime, to: LocalTime)

object Availability {

  implicit val format = Json.format[Availability]
}

sealed trait DriveStatus
case object Available extends DriveStatus
case object Unavailable extends DriveStatus

object DriveStatus {

  private val available: String = "available"
  private val unavailable: String = "unavailable"

  implicit val format = new Format[DriveStatus] {

    override def reads(json: JsValue): JsResult[DriveStatus] = json match {
      case JsString(`available`)   => JsSuccess(Available)
      case JsString(`unavailable`) => JsSuccess(Unavailable)
      case _                       => JsError(s"Unable to parse DriveStatus from $json")
    }

    override def writes(o: DriveStatus): JsValue = o match {
      case Available   => JsString(available)
      case Unavailable => JsString(unavailable)
    }
  }
}

case class DriveCreate(
                        address: Address,
                        availability: List[Availability],
                        price: Positive,
                        spaces: Positive
                      ) {

  def toDrive(pid: PID, account: Account): Drive =
    Drive(pid, address, availability, price, spaces, account.id, Available)
}

object DriveCreate {

  implicit val reads = Json.reads[DriveCreate]
}

case class Drive(
                  pid: PID,
                  address: Address,
                  availability: List[Availability],
                  price: Positive,
                  spaces: Positive,
                  accountId: UUID,
                  status: DriveStatus
                )

object Drive {

  implicit val format = Json.format[Drive]
}
