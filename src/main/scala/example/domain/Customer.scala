package example.domain

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

final case class Customer(name: String, id: Option[UUID] = None)

trait UUIDJson extends SprayJsonSupport with DefaultJsonProtocol {
  implicit object UUIDJsonFormat extends JsonFormat[UUID] {
    def write(uuid: UUID): JsValue = JsString(uuid.toString)

    def read(value: JsValue): UUID = value match {
      case JsString(x) => UUID.fromString(x)
      case x => deserializationError("Expected UUID as JsString, but got " + x)
    }
  }
}

trait CustomerJson extends UUIDJson {
  implicit val customerFormat = jsonFormat2(Customer)
}
