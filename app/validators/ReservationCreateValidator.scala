package validators

import cats.data.ValidatedNel
import cats.implicits._
import com.google.inject.{Inject, Singleton}
import domain.ReservationCreate
import domain.errors.ReservationError
import play.api.i18n.Messages

import scala.concurrent.duration.Duration

@Singleton
class ReservationCreateValidator @Inject()() {

  def validate(create: ReservationCreate)(implicit messages: Messages): ValidatedNel[ReservationError, ReservationCreate] =
    if (create.duration < Duration.Zero) ReservationError.negativeDuration.invalidNel
    else create.validNel
}