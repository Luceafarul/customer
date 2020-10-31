package example.repository

import example.database.DatabaseService
import example.domain.{Customer, CustomerTable}

import scala.concurrent.Future

class CustomerRepository(val databaseService: DatabaseService) extends Repository[Customer]
  with CustomerTable {

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
