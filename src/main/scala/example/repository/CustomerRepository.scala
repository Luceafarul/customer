package example.repository

import java.util.UUID

import example.database.PostgresService
import example.domain.Customer

import scala.concurrent.Future

class CustomerRepository(val databaseService: PostgresService) extends CustomerTable {
  import slick.jdbc.PostgresProfile.api._

  def all: Future[Seq[Customer]] =
    databaseService.db.run(customers.result)

  def create(customer: Customer): Future[Customer] =
    databaseService.db.run(customers returning customers += customer)

  def findById(id: Long): Future[Option[Customer]] =
    databaseService.db.run(customers.filter(_.id === id).result.headOption)

  def delete(id: Long): Future[Int] =
    databaseService.db.run(customers.filter(_.id === id).delete)
}
