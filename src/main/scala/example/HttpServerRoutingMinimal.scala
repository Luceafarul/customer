package example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import example.database.PostgresService
import example.repository.CustomerRepository
import example.service.CustomerService
import example.web.CustomerController

import scala.io.StdIn

object HttpServerRoutingMinimal extends App {
  implicit val system = ActorSystem("consumer-system")
  implicit val executionContext = system.dispatcher

  val databaseService = new PostgresService

  val customerRepository = new CustomerRepository(databaseService)

  val routes = CustomerController(CustomerService(customerRepository))

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(routes.route)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

  StdIn.readLine()
  bindingFuture
    .flatMap(serverBinding => serverBinding.unbind())
    .onComplete(_ => system.terminate())
}
