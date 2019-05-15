package controllers

import com.google.inject.Inject
import controllers.actions.AuthAction
import domain.{PID, ReservationCreate}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import services.ReservationService
import validators.ReservationCreateValidator

import scala.concurrent.{ExecutionContext, Future}

class ReservationController @Inject()(
                                       validator: ReservationCreateValidator,
                                       service: ReservationService,
                                       authorised: AuthAction,
                                       cc: ControllerComponents
                                     )(implicit ec: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  def getActive(pid: PID): Action[AnyContent] = (Action andThen authorised).async {

    service.getActive(pid).map(r => Ok(Json.toJson(r)))
  }

  def getAll(pid: PID): Action[AnyContent] = (Action andThen authorised).async {

    service.getAll(pid).map(r => Ok(Json.toJson(r)))
  }

  def reserve(pid: PID): Action[ReservationCreate] =
    (Action(parse.json[ReservationCreate]) andThen authorised).async { implicit request =>

      validator.validate(request.body).fold(
        errors  => Future.successful(BadRequest(Json.toJson(errors))),
        success => service.reserve(pid, request.account, success).map(a => a.fold(
          errors      => BadRequest(Json.toJson(errors)),
          reservation => Created(Json.toJson(reservation))
        ))
      )
    }
}