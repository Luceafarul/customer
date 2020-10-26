package example.web

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import example.domain.{Customer, CustomerJson}
import example.service.CustomerService

import scala.concurrent.Future

case class Routes(private val customerService: CustomerService) extends Directives with CustomerJson {
  def route: Route = concat(
    get {
      pathPrefix("customers" / JavaUUID) { id =>
        onSuccess(customerService.customer(id)) {
          case Some(customer) => complete(customer)
          case None => complete(StatusCodes.NotFound)
        }
      }
    },
    post {
      path("customers") {
        entity(as[Customer]) { customer =>
          val created: Future[Customer] = customerService.create(customer)
          onSuccess(created) { c =>
            complete(c)
          }
        }
      }
    }
  )
}
