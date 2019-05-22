import java.time.{Instant, LocalDateTime, ZoneOffset}

import play.api.libs.json.{Format, JsResult, JsSuccess, JsValue, Json}

package object repositories {

  implicit val dateTimeFormat = new Format[LocalDateTime] {
    override def writes(o: LocalDateTime): JsValue =
      Json.obj("$date" -> o.atZone(ZoneOffset.UTC).toInstant.toEpochMilli)

    override def reads(json: JsValue): JsResult[LocalDateTime] =
      JsSuccess(LocalDateTime.ofInstant(Instant.ofEpochMilli((json \ "$date").as[Long]), ZoneOffset.UTC))
  }
}