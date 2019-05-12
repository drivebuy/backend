import play.api.libs.json._

package object domain {

  sealed abstract case class Positive(value: BigDecimal)

  object Positive {

    implicit val format: Format[Positive] = new Format[Positive] {

      override def reads(json: JsValue): JsResult[Positive] = json match {
        case JsNumber(value) => Positive(value).map(JsSuccess(_)).getOrElse(JsError(""))
        case _               => JsError(s"Unable to parse Positive from $json")
      }

      override def writes(o: Positive): JsValue =
        Json.toJson(o.value.toDouble)
    }

    def apply(value: BigDecimal): Option[Positive] = {
      if (value >= 0) Some(new Positive(value) {})
      else None
    }
  }

  sealed trait DayOfWeek
  case object Monday extends DayOfWeek
  case object Tuesday extends DayOfWeek
  case object Wednesday extends DayOfWeek
  case object Thursday extends DayOfWeek
  case object Friday extends DayOfWeek
  case object Saturday extends DayOfWeek
  case object Sunday extends DayOfWeek

  object DayOfWeek {

    private val monday: String = "monday"
    private val tuesday: String = "tuesday"
    private val wednesday: String = "wednesday"
    private val thursday: String = "thursday"
    private val friday: String = "friday"
    private val saturday: String = "saturday"
    private val sunday: String = "sunday"

    implicit val format = new Format[DayOfWeek] {

      override def reads(json: JsValue): JsResult[DayOfWeek] = json match {
        case JsString(`monday`)    => JsSuccess(Monday)
        case JsString(`tuesday`)   => JsSuccess(Tuesday)
        case JsString(`wednesday`) => JsSuccess(Wednesday)
        case JsString(`thursday`)  => JsSuccess(Thursday)
        case JsString(`friday`)    => JsSuccess(Friday)
        case JsString(`saturday`)  => JsSuccess(Saturday)
        case JsString(`sunday`)    => JsSuccess(Sunday)
        case _                     => JsError(s"Unable to parse DayOfWeek from $json")
      }

      override def writes(o: DayOfWeek): JsValue = o match {
        case Monday    => JsString(monday)
        case Tuesday   => JsString(tuesday)
        case Wednesday => JsString(wednesday)
        case Thursday  => JsString(thursday)
        case Friday    => JsString(friday)
        case Saturday  => JsString(saturday)
        case Sunday    => JsString(sunday)
      }
    }
  }
}