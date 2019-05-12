package controllers

import com.google.inject.Inject
import controllers.actions.AuthAction
import domain.DriveCreate
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, ControllerComponents}
import services.DriveService
import validators.DriveCreateValidator

import scala.concurrent.{ExecutionContext, Future}

class DriveController @Inject()(service: DriveService,
                                validator: DriveCreateValidator,
                                authorised: AuthAction,
                                cc: ControllerComponents)(implicit ec: ExecutionContext, messagesApi: MessagesApi)
  extends AbstractController(cc) with I18nSupport {

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