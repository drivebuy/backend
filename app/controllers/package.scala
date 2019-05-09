import cats.data.NonEmptyList
import play.api.libs.json.{Json, Writes}

package object controllers {

  implicit def nelWrites[A: Writes]: Writes[NonEmptyList[A]] = (o: NonEmptyList[A]) => Json.toJson(o.toList)
}