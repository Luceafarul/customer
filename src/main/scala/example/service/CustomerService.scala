package example.service

import example.domain.Customer
import example.repository.CustomerRepository

import scala.concurrent.Future

class CustomerService(customerRepository: CustomerRepository) {
  def customer(id: Long): Future[Option[Customer]] =
    customerRepository.findById(id)

  def create(customer: Customer): Future[Customer] =
    customerRepository.create(customer)
}
