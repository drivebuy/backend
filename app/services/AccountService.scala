package services

import java.util.UUID

import cats.data.{OptionT, ValidatedNel}
import cats.implicits._
import com.google.inject.{ImplementedBy, Inject}
import domain.errors.AccountError
import domain.{Account, AccountCreate}
import play.api.i18n.Messages
import repositories.AccountRepository

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AccountServiceImpl])
trait AccountService {

  def login(email: String, password: String): Future[Option[UUID]]

  def create(account: AccountCreate)(implicit messages: Messages): Future[ValidatedNel[AccountError, Unit]]

  def fetchSession(sessionId: UUID): Future[Option[Account]]
}

class AccountServiceImpl @Inject()(repo: AccountRepository)(implicit ec: ExecutionContext)
  extends AccountService {

  override def login(email: String, password: String): Future[Option[UUID]] =
    (for {
      _ <- OptionT(repo.get(email, password))
      id = UUID.randomUUID()
      _ <- OptionT.liftF(repo.newSession(email, id))
    } yield id).value

  override def create(account: AccountCreate)(implicit messages: Messages): Future[ValidatedNel[AccountError, Unit]] = {
    repo.checkEmail(account.email).flatMap {
      case true  => Future.successful(AccountError.Email.duplicate.invalidNel[Unit])
      case false => repo.create(account.asAccount).map(_.validNel[AccountError])
    }
  }

  override def fetchSession(sessionId: UUID): Future[Option[Account]] = {
    repo.getBySession(sessionId)
  }
}