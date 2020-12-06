package example.web

import akka.http.scaladsl.server.{Directives, Route}
import example.service.{CustomerService, PostService}


class Api(private val customerService: CustomerService, private val postService: PostService)
  extends Directives {

  private val customerController = new CustomerController(customerService)

  private val postController = new PostController(postService)

  val route: Route = concat(customerController.route, postController.route)
}
