package example.repository

import scala.concurrent.Future

trait Repository[T] {
  def all: Future[Seq[T]]

  def create(value: T): Future[T]

  def findById(id: Long): Future[Option[T]]

  def delete(id: Long): Future[Int]
}
