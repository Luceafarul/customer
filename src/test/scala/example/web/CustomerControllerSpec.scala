package example.web

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.Materializer
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import example.config.Config
import example.database.{DatabaseService, FlywayService}
import example.domain.Customer
import example.repository.CustomerRepository
import example.service.CustomerService
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, stats}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.{AnyWordSpec, AsyncWordSpec}
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}

class CustomerControllerSpec extends AnyWordSpec
  with Matchers
  with ScalatestRouteTest
  with BeforeAndAfterAll
  with Config
  with MockFactory
  with ForAllTestContainer {

  override val container: PostgreSQLContainer = PostgreSQLContainer(
    dockerImageNameOverride = "postgres:9.6",
    databaseName = "customer_test"
  )
  container.start()

  private val flywayService = new FlywayService(container.jdbcUrl, container.username, container.password)
  private val databaseService = new DatabaseService {
    override def db: Database = Database.forURL(container.jdbcUrl, container.username, container.password)

    db.createSession()
  }
  private val customerRepository = new CustomerRepository(databaseService)
  private val customerService = new CustomerService(customerRepository)
  private val customerController = new CustomerController(customerService)
  private val route = customerController.route

  private val customerServiceStub = stub[CustomerService]
  private val customerControllerWithStub = new CustomerController(customerServiceStub)
  private val routeWithStub = customerControllerWithStub.route

  private val customer = Customer("Marcus Aurelius")
  private val existedCustomer = Customer("Marcus Aurelius", Some(1L))
  private val existedCustomerId = existedCustomer.id.get

  override protected def beforeAll(): Unit =
    flywayService.migrateDatabase

  override protected def afterAll(): Unit =
    flywayService.dropDatabase

  "The Customer Controller" should {
    "return NotFound if customer does not exist" in {
      val notExistedId = 777

      Get(s"/customers/$notExistedId") ~> route ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    "return customer by id if existing" in {
      val createdCustomer = Post("/customers", customer) ~> route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Customer]
      }

      Get(s"/customers/${createdCustomer.id.get}") ~> route ~> check {
        status shouldBe StatusCodes.OK

        val foundCustomer = responseAs[Customer]
        foundCustomer shouldBe createdCustomer
      }
    }

    "return created customer" in {
      Post("/customers", customer) ~> route ~> check {
        status shouldBe StatusCodes.OK

        val created = responseAs[Customer]
        created.name shouldBe customer.name
        created.id shouldBe defined
      }
    }

    "deleted existed customer by id" in {
      val createdCustomer = Post("/customers", customer) ~> route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Customer]
      }

      Delete(s"/customers/${createdCustomer.id.get}") ~> route ~> check {
        status shouldBe StatusCodes.NoContent
      }

      Get(s"/customers/${createdCustomer.id.get}") ~> route ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    "return BadRequest when failed retrieve customer" in {
      (customerServiceStub.get _)
        .when(existedCustomerId)
        .returns(Future.failed(new UnsupportedOperationException))

      Get(s"/customers/$existedCustomerId") ~> Route.seal(routeWithStub) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "return InternalServerError when failed retrieve customer" in {
      (customerServiceStub.get _)
        .when(existedCustomerId)
        .returns(Future.failed(new OutOfMemoryError("Exception for tests")))

      Get(s"/customers/$existedCustomerId") ~> Route.seal(routeWithStub) ~> check {
        status shouldBe StatusCodes.InternalServerError
      }
    }

    "return BadRequest when failed create customer" in {
      (customerServiceStub.create _)
        .when(customer)
        .returns(Future.failed(new UnsupportedOperationException))

      Post("/customers", customer) ~> Route.seal(routeWithStub) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "return InternalServerError when failed to create customer" in {
      (customerServiceStub.create _)
        .when(customer)
        .returns(Future.failed(new OutOfMemoryError("Exception for tests")))

      Post("/customers", customer) ~> Route.seal(routeWithStub) ~> check {
        status shouldBe StatusCodes.InternalServerError
      }
    }

    "return BadRequest when failed to delete customer" in {
      (customerServiceStub.delete _)
        .when(existedCustomerId)
        .returns(Future.failed(new UnsupportedOperationException))

      Delete(s"/customers/$existedCustomerId") ~> Route.seal(routeWithStub) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "return InternalServerError when failed to delete customer" in {
      (customerServiceStub.delete _)
        .when(existedCustomerId)
        .returns(Future.failed(new OutOfMemoryError("Exception for tests")))

      Delete(s"/customers/$existedCustomerId") ~> Route.seal(routeWithStub) ~> check {
        status shouldBe StatusCodes.InternalServerError
      }
    }
  }
}
