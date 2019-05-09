package controllers

import com.google.inject.Inject
import controllers.actions.AuthAction
import domain.errors.AccountError
import domain.{AccountCreate, AccountCreated, AccountLogin, AccountSession}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import services.AccountService
import validators.AccountCreateValidator

import scala.concurrent.{ExecutionContext, Future}

class AccountController @Inject()(validator: AccountCreateValidator,
                                  service: AccountService,
                                  authorised: AuthAction,
                                  cc: ControllerComponents)
                                 (implicit ec: ExecutionContext, override val messagesApi: MessagesApi)
  extends AbstractController(cc) with I18nSupport {

  def create: Action[AccountCreate] = Action(parse.json[AccountCreate]).async { implicit request =>
    validator.validate(request.body).fold(
      errors  => Future.successful(BadRequest(Json.toJson(errors))),
      account => service.create(account).map(a => a.fold(
          errors => BadRequest(Json.toJson(errors)),
          _      => Created(Json.toJson(AccountCreated.success))
        )
      )
    )
  }

  def login: Action[AccountLogin] = Action(parse.json[AccountLogin]).async { implicit request =>
    service.login(request.body.email, request.body.password).map {
      case Some(session) => Ok(Json.toJson(AccountSession(session)))
      case None          => BadRequest(Json.toJson(AccountError.invalidCreds))
    }
  }

  def protectedTest: Action[AnyContent] = (Action andThen authorised) {
    Ok(Json.toJson("Success"))
  }
}