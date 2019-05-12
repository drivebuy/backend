package services

import cats.data.{OptionT, ValidatedNel}
import cats.implicits._
import com.google.inject.ImplementedBy
import domain.errors.DriveError
import domain.{Account, Drive, DriveCreate, PID}
import javax.inject.Inject
import play.api.i18n.Messages
import repositories.{DriveRepository, PIDRepository}

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[DriveServiceImpl])
trait DriveService {

  def get(pid: PID): Future[Option[Drive]]

  def create(driveCreate: DriveCreate, account: Account)(implicit messages: Messages): Future[ValidatedNel[DriveError, Drive]]
}

class DriveServiceImpl @Inject()(driveRepo: DriveRepository, pidRepo: PIDRepository)(implicit ec: ExecutionContext)
  extends DriveService {

  override def get(pid: PID): Future[Option[Drive]] =
    driveRepo.get(pid)

  override def create(driveCreate: DriveCreate, account: Account)(implicit messages: Messages): Future[ValidatedNel[DriveError, Drive]] =
    (for {
      pid   <- OptionT(pidRepo.next())
      drive  = driveCreate.toDrive(pid, account)
      _     <- OptionT.liftF(driveRepo.create(drive))
    } yield {
      drive
    }).value.map(_.map(_.validNel).getOrElse(DriveError.unableToAssignPID.invalidNel))
}