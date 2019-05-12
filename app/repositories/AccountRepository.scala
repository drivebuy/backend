package repositories

import java.util.UUID

import com.google.inject.{ImplementedBy, Inject}
import domain.{Account, Password}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.ReadConcern
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[MongoAccountRepository])
trait AccountRepository {

  def get(email: String, password: String): Future[Option[Account]]

  def create(account: Account): Future[Unit]

  def checkEmail(email: String): Future[Boolean]

  def getBySession(sessionId: UUID): Future[Option[Account]]

  def newSession(email: String, sessionId: UUID): Future[Unit]
}

class MongoAccountRepository @Inject()(mongo: ReactiveMongoApi)(implicit ex: ExecutionContext)
  extends AccountRepository {


  private val collectionName: String = "accounts"
  private val idField = "id"
  private val emailField = "email"
  private val sessionField = "sessions"

  private def collection: Future[JSONCollection] =
    mongo.database.map(_.collection[JSONCollection](collectionName))

  private val index = Index(
    key = Seq((idField, IndexType.Ascending)),
    name = Some(s"${idField}_index"),
    unique = true
  )

  val started: Future[Boolean] = collection.flatMap(_.indexesManager.ensure(index))

  override def get(email: String, password: String): Future[Option[Account]] = {

    val selector = Json.obj(emailField -> email)

    getAccount(collection, selector)
      .map(_.filter{account => account.password == Password(password, account.password.salt)})
  }

  override def create(account: Account): Future[Unit] = {
    collection.flatMap {
      _.insert(false).one(account).map(_ => ())
    }
  }

  override def checkEmail(email: String): Future[Boolean] = {

    val selector = Json.obj(emailField -> email)

    exists(collection, selector)
  }

  override def newSession(email: String, sessionId: UUID): Future[Unit] = {

    val selector = Json.obj(emailField -> email)

    val updater = Json.obj(
      "$push" -> Json.obj(
        sessionField -> Json.toJson(sessionId)
      )
    )

    collection.flatMap(_.findAndUpdate(selector, updater).map(_ => ()))
  }

  override def getBySession(sessionId: UUID): Future[Option[Account]] = {

    val selector = Json.obj(
      sessionField -> Json.obj(
        "$elemMatch" -> Json.obj("$eq" -> Json.toJson(sessionId))
      )
    )

    getAccount(collection, selector)
  }

  private def exists(collection: Future[JSONCollection], selector: JsObject): Future[Boolean] = {
    collection
      .flatMap(_.count(Some(selector), Some(1), 0, None, ReadConcern.Available))
      .map(_ == 1)
  }

  private def getAccount(collection: Future[JSONCollection], selector: JsObject): Future[Option[Account]] = {
    collection.flatMap(_.find(selector, None).one[JsValue])
      .map(_.flatMap(Json.fromJson[Account](_).asOpt))
  }
}