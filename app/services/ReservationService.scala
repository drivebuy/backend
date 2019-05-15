package services

import java.time.LocalDateTime

import cats.data._
import cats.implicits._
import com.google.inject.{ImplementedBy, Inject}
import domain.errors.ReservationError
import domain.{Account, Available, PID, Reservation, ReservationCreate, Reservations}
import play.api.i18n.Messages
import repositories.ReservationRepository

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[ReservationServiceImpl])
trait ReservationService {

  def getActive(pid: PID): Future[Reservations]

  def getAll(pid: PID): Future[Reservations]

  def reserve(pid: PID, account: Account, create: ReservationCreate)(implicit messages: Messages)
    : Future[ValidatedNel[ReservationError, Reservations]]
}

class ReservationServiceImpl @Inject()(driveService: DriveService, repo: ReservationRepository)(implicit ec: ExecutionContext) extends ReservationService {

  def getActive(pid: PID): Future[Reservations] =
    repo.getActive(pid)

  def getAll(pid: PID): Future[Reservations] =
    repo.getAll(pid)

  def reserve(pid: PID, account: Account, create: ReservationCreate)(implicit messages: Messages)
    : Future[ValidatedNel[ReservationError, Reservations]] = {

    (for {
      drive      <- OptionT(driveService.get(pid))
      now         = LocalDateTime.now()
      reservation = Reservation(account.id, now, now.plusSeconds(create.duration.toSeconds))
      result     <- OptionT(repo.reserve(pid, reservation).map(_ => Reservations(pid, List(reservation)).some))
    } yield {
      if (drive.status == Available) result.validNel
      else ReservationError.invalidPeriod.invalidNel
    }).getOrElse(ReservationError.invalidPID.invalidNel)

  }
}
