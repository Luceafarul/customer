package example.web

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import com.typesafe.scalalogging.Logger
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import example.domain.Customer
import example.service.CustomerService
import io.circe.generic.auto._

import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

class CustomerController(private val customerService: CustomerService) extends Directives
  with FailFastCirceSupport
  with ErrorHandling {

  private val log = Logger[CustomerController]

  def route: Route = concat(
    get {
      path("api" / "customers" / LongNumber) { id =>
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
      path("api" / "customers") {
        entity(as[Customer]) { customer =>
          val created: Future[Customer] = customerService.create(customer)
          onComplete(created) {
            case Success(c) => complete(c)
            case Failure(e) if NonFatal(e.getCause) => logAndReturnBadRequest(e)
            case Failure(e) => logAndReturnServerError(e)
          }
        }
      }
    },
    delete {
      path("api" / "customers" / LongNumber) { id =>
        onComplete(customerService.delete(id)) {
          case Success(_) => complete(StatusCodes.NoContent)
          case Failure(e) if NonFatal(e.getCause) => logAndReturnBadRequest(e)
          case Failure(e) => logAndReturnServerError(e)
        }
      }
    }
  )
}
