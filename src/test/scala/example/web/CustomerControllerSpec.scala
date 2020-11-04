package example.web

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import example.config.Config
import example.database.{DatabaseService, FlywayService}
import example.domain.Customer
import example.repository.CustomerRepository
import example.service.CustomerService
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.Future

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
      val customer = Customer("Marcus Aurelius")

      for {
        createdCustomer <- customerRepository.create(customer)
      } yield {
        Get(s"/customer/${createdCustomer.id.get}") ~> route ~> check {
          status shouldBe StatusCodes.OK
          val foundCustomer = responseAs[Customer]
          foundCustomer shouldBe createdCustomer
        }
      }
    }

    "return created customer" in {
      val customer = Customer("Marcus Aurelius")

      Post("/customers", customer) ~> route ~> check {
        status shouldBe StatusCodes.OK

        val created = responseAs[Customer]
        created.name shouldBe customer.name
        created.id shouldBe defined
      }
    }

    "return BadRequest when failed create customer" in {
      val customer = Customer("Marcus Aurelius")

      (customerServiceStub.create _)
        .when(customer)
        .returns(Future.failed(new UnsupportedOperationException))

      Post("/customers", customer) ~> Route.seal(routeWithStub) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "return BadRequest when failed retrieve customer" in {
      val customer = Customer("Marcus Aurelius", Some(1L))
      val customerId = customer.id.get

      (customerServiceStub.get _)
        .when(customerId)
        .returns(Future.failed(new UnsupportedOperationException))

      Get(s"/customers/$customerId") ~> Route.seal(routeWithStub) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }
  }
}
