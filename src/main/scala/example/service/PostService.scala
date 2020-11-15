package example.service

import example.domain.Post
import example.repository.PostRepository

import scala.concurrent.Future

class PostService(repository: PostRepository) {
  def getById(id: Long): Future[Option[Post]] =
    repository.findById(id)

  def getAllByCustomerId(customerId: Long): Future[Seq[Post]] =
    repository.allByCustomerId(customerId)

  def create(post: Post): Future[Post] =
    repository.create(post)

  def delete(id: Long): Future[Int] =
    repository.delete(id)
}
