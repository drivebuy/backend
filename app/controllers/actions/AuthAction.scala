package controllers.actions

import java.util.UUID

import cats.data.OptionT
import cats.implicits._
import com.google.inject.Inject
import domain.Account
import domain.errors.HttpError
import play.api.mvc.{ActionRefiner, Request, Result, WrappedRequest}
import services.AccountService
import play.api.http.HeaderNames.AUTHORIZATION
import play.api.libs.json.Json
import play.api.mvc.Results._

import scala.concurrent.{ExecutionContext, Future}

case class AuthRequest[A](request: Request[A], sessionId: UUID, account: Account) extends WrappedRequest[A](request)

class AuthAction @Inject()(service: AccountService)(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[Request, AuthRequest] {

  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthRequest[A]]] =
    OptionT.fromOption[Future](request.headers.get(AUTHORIZATION))
      .flatMap { sessionId =>

        val uuid = UUID.fromString(sessionId.replace("Bearer ", ""))
        OptionT(service.fetchSession(uuid)).map((uuid, _))
      }
      .map(t => AuthRequest(request, t._1, t._2).asRight)
      .getOrElse(Unauthorized(Json.toJson(HttpError.unauthorised)).asLeft)

}