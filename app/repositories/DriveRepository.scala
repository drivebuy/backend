package repositories

import com.google.inject.{ImplementedBy, Inject}
import domain.{Drive, PID}
import play.api.libs.json.{JsValue, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[MongoDriveRepository])
trait DriveRepository {

  def get(pid: PID): Future[Option[Drive]]

  def create(drive: Drive): Future[Unit]
}

class MongoDriveRepository @Inject()(mongo: ReactiveMongoApi)(implicit ex: ExecutionContext) extends DriveRepository {

  private val collectionName: String = "drives"
  private val pidField = "pid"

  private def collection: Future[JSONCollection] =
    mongo.database.map(_.collection[JSONCollection](collectionName))

  private val index = Index(
    key = Seq((pidField, IndexType.Ascending)),
    name = Some(s"${pidField}_index"),
    unique = true
  )

  val started: Future[Boolean] = collection.flatMap(_.indexesManager.ensure(index))

  override def get(pid: PID): Future[Option[Drive]] = {

    val selector = Json.obj(pidField -> pid.value)

    collection.flatMap(_.find(selector, None).one[JsValue])
      .map(_.flatMap(Json.fromJson[Drive](_).asOpt))
  }

  override def create(drive: Drive): Future[Unit] = {

    collection.flatMap {
      _.insert(false).one(drive).map(_ => ())
    }
  }
}