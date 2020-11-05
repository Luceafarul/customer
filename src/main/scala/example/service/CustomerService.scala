package example.service

import example.domain.Customer
import example.repository.Repository

import scala.concurrent.Future

class CustomerService(repository: Repository[Customer]) {
  def get(id: Long): Future[Option[Customer]] =
    repository.findById(id)

  def create(customer: Customer): Future[Customer] =
    repository.create(customer)

  def delete(id: Long): Future[Int] =
    repository.delete(id)
}
