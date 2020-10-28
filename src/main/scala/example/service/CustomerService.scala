package example.service

import java.util.UUID

import example.domain.Customer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final case class CustomerService() {
  def customer(id: Long): Future[Option[Customer]] = Future {
    Some(Customer("John Doe", Some(id)))
  }

  def create(customer: Customer): Future[Customer] = Future {
    Customer(customer.name, Some(scala.util.Random.nextLong))
  }
}
