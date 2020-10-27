package example.web

import java.util.UUID

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import example.domain.{Customer, CustomerJson}
import example.service.CustomerService
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CustomerSpec extends AnyWordSpec with Matchers with ScalatestRouteTest with CustomerJson {
  private val customerService = CustomerService()
  private val customerController = CustomerController(customerService)
  private val route = customerController.route

  "The Customer Controller" should {
    "return random customer with passed uuid" in {
      val id = UUID.randomUUID()

      Get(s"/customers/$id") ~> route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Customer] shouldBe Customer("John Doe", Some(id))
      }
    }

    "return error when we passed wrong id format" in {
      val id = 10

      Get(s"/customers/$id") ~> route ~> check {
        handled shouldBe false
      }
    }

    "return created customer" in {
      val customer = Customer("Marcus Aurelius")

      Post(s"/customers", customer) ~> route ~> check {
        status shouldBe StatusCodes.OK

        val created = responseAs[Customer]
        created.name shouldBe customer.name
        created.id shouldBe defined
      }
    }
  }
}
