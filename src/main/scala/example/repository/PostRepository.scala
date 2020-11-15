package example.repository

import example.database.DatabaseService
import example.domain.{Post, PostTable}

import scala.concurrent.Future

class PostRepository(databaseService: DatabaseService) extends Repository[Post] with PostTable {
  import slick.jdbc.PostgresProfile.api._

  override def all: Future[Seq[Post]] = ???

  override def create(value: Post): Future[Post] = ???

  override def findById(id: Long): Future[Option[Post]] = ???

  override def delete(id: Long): Future[Int] = ???
}
