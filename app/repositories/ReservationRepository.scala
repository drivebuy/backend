package repositories

import java.time.LocalDateTime

import com.google.inject.{ImplementedBy, Inject}
import domain.{PID, Reservation, Reservations}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
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

  private val collectionName = "reservations"
  private val pidField = "pid"
  private val reservations = "reservations"
  private val endTime = "endTime"

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
      pidField -> pid.value
    )

    val projection = Json.obj(
      reservations -> Json.arr(
        Json.obj(
          "$filter" -> Json.obj(
            "input" -> s"$$$reservations",
            "as" -> "item",
            "cond" -> Json.obj(
              "$gt" -> Json.arr(
                "$$item.endTime",
                Json.toJson(LocalDateTime.now())(dateTimeFormat)
              )
            )
          )
        )
      )
    )

    collection.flatMap { col =>

      col.find(selector, Some(projection)).one[JsValue].map { jOpt =>

        val reservations = jOpt.flatMap(json => (json \ "reservations").asOpt[List[Reservation]])
        reservations.map(x => Reservations(pid, x)).getOrElse(Reservations.empty(pid))
      }
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
        reservations -> Json.toJson(reservation)
      )
    )

    collection.flatMap(_.update(false).one(selector, modifier, upsert = true).map(_ => ()))

  }
}