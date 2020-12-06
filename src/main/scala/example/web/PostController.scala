package example.web

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route, StandardRoute}
import com.typesafe.scalalogging.Logger
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import example.domain.Post
import example.service.PostService
import io.circe.generic.auto._

import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

class PostController(private val postService: PostService) extends Directives
  with FailFastCirceSupport {

  private val log = Logger[PostController]

  val route: Route = concat(
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

  // TODO how to remove duplication???
  private def logAndReturnBadRequest(e: Throwable): StandardRoute = {
    log.error(s"Failed with: ${e.getMessage}")
    complete(StatusCodes.BadRequest, s"Request failed with: ${e.getMessage}")
  }

  private def logAndReturnServerError(e: Throwable): StandardRoute = {
    log.error(s"Server error: ${e.getCause.getMessage}")
    complete(StatusCodes.InternalServerError, s"Request failed with: ${e.getCause.getMessage}")
  }
}
