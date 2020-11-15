package example.domain

import java.sql.Timestamp
import java.time.LocalDateTime

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape
import spray.json._

final case class Post(content: String, createdAt: LocalDateTime, userId: Long, id: Option[Long] = None)

object Post extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object LocalDateTimeJsonFormat extends RootJsonFormat[LocalDateTime] {
    override def read(json: JsValue): LocalDateTime = json match {
      case JsString(value) => Timestamp.valueOf(value).toLocalDateTime
      case _ => throw DeserializationException("Timestamp expected")
    }

    override def write(localDateTime: LocalDateTime): JsValue =
      JsString(Timestamp.valueOf(localDateTime).toString)
  }

  implicit val postFormat = jsonFormat4(Post.apply)
}

trait PostTable {

  class Posts(tag: Tag) extends Table[Post](tag, "posts") {
    def id: Rep[Option[Long]] = column[Option[Long]]("id", O.AutoInc, O.PrimaryKey)

    def content: Rep[String] = column[String]("content")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

    def userId: Rep[Long] = column[Long]("user_id")

    override def * : ProvenShape[Post] = (content, createdAt, userId, id) <> ((Post.apply _).tupled, Post.unapply)
  }

  protected val posts = TableQuery[Posts]
}
