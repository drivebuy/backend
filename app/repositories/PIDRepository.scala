package repositories

import com.google.inject.{ImplementedBy, Inject}
import domain.PID
import play.api.libs.json.{JsNumber, JsValue, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[MongoPIDRepository])
trait PIDRepository {

  def next(): Future[Option[PID]]
}

class MongoPIDRepository @Inject()(mongo: ReactiveMongoApi)(implicit ex: ExecutionContext) extends PIDRepository {

  private val collectionName: String = "pids"
  private val idField  = "id"
  private val idValue  = "current"
  private val pidField = "pid"

  private def collection: Future[JSONCollection] =
    mongo.database.map(_.collection[JSONCollection](collectionName))

  private val index = Index(
    key = Seq((pidField, IndexType.Ascending)),
    name = Some(s"${pidField}_index"),
    unique = true
  )

  val started: Future[Unit] = for {
    col <- collection
    _   <- col.indexesManager.ensure(index)
    _   <- col.update(false).one(
        Json.obj(idField -> idValue),
        Json.obj(
          "$setOnInsert" -> Json.obj(pidField -> 1)
        ),
        upsert = true
      )
  } yield ()

  override def next(): Future[Option[PID]] = {
    val selector = Json.obj(idField -> idValue)

    val modifier = Json.obj(
      "$inc" -> Json.obj(pidField -> 1)
    )

    collection.flatMap {
      _.findAndUpdate(selector, modifier, upsert = true).map(_.result[JsValue].flatMap { json =>
        (json \ pidField).toOption.collect {
          case JsNumber(value) if value.isValidInt => PID(value.toInt)
        }
      })
    }
  }
}