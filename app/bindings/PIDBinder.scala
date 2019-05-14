package bindings

import domain.PID
import play.api.mvc.PathBindable

import scala.util.Try

object PIDBinder {

  implicit def pathBinder(implicit stringBinder: PathBindable[String]): PathBindable[PID] = new PathBindable[PID] {
    override def bind(key: String, value: String): Either[String, PID] = {
      Try(value.toInt).fold(
        _ => Left("Unable to parse PID"),
        i => Right(PID(i))
      )
    }

    override def unbind(key: String, value: PID): String =
      value.value
  }
}