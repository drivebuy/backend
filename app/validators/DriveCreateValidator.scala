package validators

import cats.data.ValidatedNel
import cats.implicits._
import com.google.inject.{Inject, Singleton}
import domain.{Address, Availability, DriveCreate}
import domain.errors.DriveError
import play.api.i18n.Messages

@Singleton
class DriveCreateValidator @Inject()() {

  def validate(drive: DriveCreate)(implicit messages: Messages): ValidatedNel[DriveError, DriveCreate] =
    drive.availability.foldLeft(validateAddress(drive.address))(_ |+| validateAvailability(_)).map(_ => drive)

  private def validateAddress(address: Address)(implicit messages: Messages): ValidatedNel[DriveError, Unit] = {
    validateStringRequired(address.firstLine, DriveError.Address.firstLineRequired) |+|
      validateStringRequired(address.postcode, DriveError.Address.postcodeRequired)
  }

  private def validateStringRequired(value: String, error: DriveError): ValidatedNel[DriveError, Unit] = {
    value match {
      case "" => error.invalidNel
      case _  => ().validNel
    }
  }

  private def validateAvailability(availability: Availability)(implicit messages: Messages): ValidatedNel[DriveError, Unit] = {
    if (availability.to.isBefore(availability.from)) DriveError.Availability.toBeforeFrom.invalidNel
    else ().validNel
  }
}