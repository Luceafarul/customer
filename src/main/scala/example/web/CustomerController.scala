package example.web

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route, StandardRoute}
import com.typesafe.scalalogging.Logger
import example.domain.{Customer, Post}
import example.service.{CustomerService, PostService}

import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

class CustomerController(private val customerService: CustomerService,
                         private val postService: PostService) extends Directives {
  private val log = Logger[CustomerController]

  def route: Route = concat(
    get {
      path("customers" / LongNumber) { id =>
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
    },
    delete {
      path("customers" / LongNumber) { id =>
        onComplete(customerService.delete(id)) {
          case Success(_) => complete(StatusCodes.NoContent)
          case Failure(e) if NonFatal(e.getCause) => logAndReturnBadRequest(e)
          case Failure(e) => logAndReturnServerError(e)
        }
      }
    },
    get {
      path("api" / "customers" / LongNumber / "posts" / LongNumber) { (customerId, postId) =>
        onComplete(postService.getByCustomerIdAndPostId(customerId, postId)) {
          case Success(post) => post match {
            case Some(p) => complete(p)
            case None => complete(StatusCodes.NotFound)
          }
          case Failure(e) if NonFatal(e.getCause) => logAndReturnBadRequest(e)
          case Failure(e) => logAndReturnServerError(e)
        }
      }
    },
    get {
      path("api" / "customers" / LongNumber / "posts") { customerId =>
        onComplete(postService.getAllByCustomerId(customerId)) {
          case Success(posts) => complete(posts)
          case Failure(e) if NonFatal(e.getCause) => logAndReturnBadRequest(e)
          case Failure(e) => logAndReturnServerError(e)
        }
      }
    },
    post {
      path("api" / "customers" / LongNumber / "posts") { customerId =>
        entity(as[Post]) { post =>
          val created: Future[Post] = postService.create(post.copy(customerId = customerId))
          onComplete(created) {
            case Success(p) => complete(p)
            case Failure(e) if NonFatal(e.getCause) => logAndReturnBadRequest(e)
            case Failure(e) => logAndReturnServerError(e)
          }
        }
      }
    },
    delete {
      path("api" / "customers" / "posts" / LongNumber) { postId =>
        onComplete(postService.delete(postId)) {
          case Success(_) => complete(StatusCodes.NoContent)
          case Failure(e) if NonFatal(e.getCause) => logAndReturnBadRequest(e)
          case Failure(e) => logAndReturnServerError(e)
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
