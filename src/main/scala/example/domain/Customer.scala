package example.domain

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape
import spray.json._

final case class Customer(name: String, id: Option[Long] = None)

object Customer extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val customerFormat = jsonFormat2(Customer.apply)
}

trait CustomerTable {

  class Customers(tag: Tag) extends Table[Customer](tag, "customers") {
    def id: Rep[Option[Long]] = column[Option[Long]]("id", O.AutoInc, O.PrimaryKey)

    def name: Rep[String] = column[String]("name")

    override def * : ProvenShape[Customer] = (name, id) <> ((Customer.apply _).tupled, Customer.unapply)
  }

  protected val customers = TableQuery[Customers]
}
