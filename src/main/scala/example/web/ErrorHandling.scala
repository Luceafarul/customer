package example.web

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.StandardRoute
import com.typesafe.scalalogging.Logger

trait ErrorHandling {
  private val log = Logger[ErrorHandling]

  protected def logAndReturnBadRequest(e: Throwable): StandardRoute = {
    log.error(s"Failed with: ${e.getMessage}")
    complete(StatusCodes.BadRequest, s"Request failed with: ${e.getMessage}")
  }

  protected def logAndReturnServerError(e: Throwable): StandardRoute = {
    log.error(s"Server error: ${e.getCause.getMessage}")
    complete(StatusCodes.InternalServerError, s"Request failed with: ${e.getCause.getMessage}")
  }
}
