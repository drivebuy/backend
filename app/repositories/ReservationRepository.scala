package repositories

import java.time.{LocalDateTime, ZoneOffset}

import com.google.inject.{ImplementedBy, Inject}
import domain.{PID, Reservation, Reservations}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[MongoReservationRepository])
trait ReservationRepository {

  def getActive(pid: PID): Future[Reservations]

  def getAll(pid: PID): Future[Reservations]

  def reserve(pid: PID, reservation: Reservation): Future[Unit]
}

class MongoReservationRepository @Inject()(mongo: ReactiveMongoApi)(implicit ec: ExecutionContext) extends ReservationRepository {

  private val collectionName: String = "reservations"
  private val pidField = "pid"
  private val reservations = "reservations"
  private val accountId = "accountId"
  private val startDate = "startDate"
  private val endDate  = "endDate"

  private def collection: Future[JSONCollection] =
    mongo.database.map(_.collection[JSONCollection](collectionName))

  private val index = Index(
    key = Seq((pidField, IndexType.Ascending)),
    name = Some(s"${pidField}_index"),
    unique = true
  )

  val started: Future[Boolean] = collection.flatMap(_.indexesManager.ensure(index))

  override def getActive(pid: PID): Future[Reservations] = {

    val selector = Json.obj(
      pidField -> pid.value,
      s"$reservations.$endDate" -> Json.obj("$gt" -> jsonDate(LocalDateTime.now()))
    )

    collection.flatMap(_.find(selector, None).one[JsValue])
      .map { jOpt =>

        val reservation = jOpt.map(_.as[Reservation])
        Reservations(pid, reservation.toList)
      }
  }

  override def getAll(pid: PID): Future[Reservations] = {

    val selector = Json.obj(
      pidField -> pid.value
    )

    collection.flatMap(_.find(selector, None).one[JsValue])
      .map(_.flatMap(_.asOpt[Reservations]).getOrElse(Reservations.empty(pid)))
  }

  override def reserve(pid: PID, reservation: Reservation): Future[Unit] = {

    val selector = Json.obj(
      pidField -> pid.value
    )

    val modifier = Json.obj(
      "$push" -> Json.obj(
        reservations -> Json.obj(
          accountId -> Json.toJson(reservation.accountId),
          startDate -> jsonDate(reservation.startTime),
          endDate   -> jsonDate(reservation.endTime)
        )
      )
    )

    collection.flatMap(_.update(false).one(selector, modifier, upsert = true).map(_ => ()))

  }

  private def jsonDate(dateTime: LocalDateTime): JsObject =
    Json.obj("$date" -> dateTime.toEpochSecond(ZoneOffset.UTC))
}