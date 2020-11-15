package example.repository

import example.database.DatabaseService
import example.domain.{Post, PostTable}

import scala.concurrent.Future

class PostRepository(databaseService: DatabaseService) extends Repository[Post] with PostTable {

  import slick.jdbc.PostgresProfile.api._

  override def all: Future[Seq[Post]] =
    databaseService.db.run(posts.result)

  override def create(post: Post): Future[Post] =
    databaseService.db.run(posts returning posts += post)

  override def findById(id: Long): Future[Option[Post]] =
    databaseService.db.run(posts.filter(_.id === id).result.headOption)

  override def delete(id: Long): Future[Int] =
    databaseService.db.run(posts.filter(_.id === id).delete)
}
