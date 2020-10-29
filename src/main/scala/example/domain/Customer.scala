package example.domain

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

final case class Customer(name: String, id: Option[Long] = None)

object Customer extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val customerFormat = jsonFormat2(Customer.apply)
}

