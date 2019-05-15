package domain

import java.time.LocalDateTime
import java.util.UUID

import play.api.libs.json.Json

import scala.concurrent.duration.Duration

case class ReservationCreate(duration: Duration)

object ReservationCreate {

  implicit val reads = Json.reads[ReservationCreate]
}

case class Reservation(accountId: UUID, startTime: LocalDateTime, endTime: LocalDateTime)

object Reservation {

  implicit val format = Json.format[Reservation]
}

case class Reservations(pid: PID, reservations: List[Reservation])

object Reservations {

  implicit val format = Json.format[Reservations]

  def empty(pid: PID): Reservations = Reservations(pid, List.empty)
}