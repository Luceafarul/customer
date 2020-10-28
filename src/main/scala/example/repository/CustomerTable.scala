package example.repository

import example.domain.Customer
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

trait CustomerTable {

  class Customers(tag: Tag) extends Table[Customer](tag, "customers") {
    def id: Rep[Option[Long]] = column[Option[Long]]("id", O.AutoInc, O.PrimaryKey)

    def name: Rep[String] = column[String]("name")

    override def * : ProvenShape[Customer] = (name, id) <> ((Customer.apply _).tupled, Customer.unapply)
  }

  protected val customers = TableQuery[Customers]
}
