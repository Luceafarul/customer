package example.web

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, ExceptionHandler, Route, StandardRoute}
import com.typesafe.scalalogging.Logger
import example.domain.Customer
import example.service.CustomerService

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.util.control.NonFatal

class CustomerController(private val customerService: CustomerService) extends Directives {
  private val log = Logger[CustomerController]

  def route: Route = concat(
    get {
      pathPrefix("customers" / LongNumber) { id =>
        onComplete(customerService.get(id)) {
          case Success(c) => c match {
            case Some(customer) => complete(customer)
            case None => complete(StatusCodes.NotFound)
          }
          case Failure(e) if NonFatal(e.getCause) => logAndReturnBadRequest(e)
          case Failure(e) => logAndReturnServerError(e)
        }
      }
    },
    post {
      path("customers") {
        entity(as[Customer]) { customer =>
          val created: Future[Customer] = customerService.create(customer)
          onComplete(created) {
            case Success(c) => complete(c)
            case Failure(e) if NonFatal(e.getCause) => logAndReturnBadRequest(e)
            case Failure(e) => logAndReturnServerError(e)
          }
        }
      }
    }
  )

  private def logAndReturnBadRequest(e: Throwable): StandardRoute = {
    log.error(s"Failed with: ${e.getMessage}")
    complete(StatusCodes.BadRequest, s"Request failed with: ${e.getMessage}")
  }

  private def logAndReturnServerError(e: Throwable): StandardRoute = {
    log.error(s"Server error: ${e.getCause.getMessage}")
    complete(StatusCodes.InternalServerError, s"Request failed with: ${e.getCause.getMessage}")
  }
}
