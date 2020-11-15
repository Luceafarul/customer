package example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import example.database.PostgresService
import example.repository.{CustomerRepository, PostRepository}
import example.service.{CustomerService, PostService}
import example.web.CustomerController

import scala.io.StdIn

object HttpServerRoutingMinimal extends App {
  implicit val system = ActorSystem("consumer-system")
  implicit val executionContext = system.dispatcher

  private val databaseService = new PostgresService

  private val customerRepository = new CustomerRepository(databaseService)

  private val postRepository = new PostRepository(databaseService)

  private val customerService = new CustomerService(customerRepository)

  private val postService = new PostService(postRepository)

  private val routes = new CustomerController(customerService, postService)

  private val bindingFuture = Http().newServerAt("localhost", 8080).bind(routes.route)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

  StdIn.readLine()
  bindingFuture
    .flatMap(serverBinding => serverBinding.unbind())
    .onComplete(_ => system.terminate())
}
