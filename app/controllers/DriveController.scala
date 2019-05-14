package controllers

import com.google.inject.Inject
import controllers.actions.AuthAction
import domain.errors.HttpError
import domain.{Drive, DriveCreate, PID}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import services.DriveService
import validators.DriveCreateValidator

import scala.concurrent.{ExecutionContext, Future}

class DriveController @Inject()(service: DriveService,
                                validator: DriveCreateValidator,
                                authorised: AuthAction,
                                cc: ControllerComponents)(implicit ec: ExecutionContext, messagesApi: MessagesApi)
  extends AbstractController(cc) with I18nSupport {

  def get(pid: PID): Action[AnyContent] = (Action andThen authorised).async { implicit request =>
    service.get(pid).map {
      case Some(drive) => Ok(Json.toJson(drive))
      case None        => NotFound(Json.toJson(HttpError.notFound))
    }
  }

  def create: Action[DriveCreate] = (Action(parse.json[DriveCreate]) andThen authorised).async { implicit request =>
    validator.validate(request.body).fold(
      errors  => Future.successful(BadRequest(Json.toJson(errors))),
      success => service.create(success, request.account).map { a =>
        a.fold(
          errors => BadRequest(Json.toJson(errors)),
          drive  => Created(Json.toJson(drive))
        )
      }
    )
  }
}