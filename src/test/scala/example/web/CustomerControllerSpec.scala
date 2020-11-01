package example.web

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import example.config.Config
import example.database.{DatabaseService, FlywayService}
import example.domain.Customer
import example.repository.CustomerRepository
import example.service.CustomerService
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import slick.jdbc.JdbcBackend.Database

class CustomerControllerSpec extends AnyWordSpec
  with Matchers
  with ScalatestRouteTest
  with BeforeAndAfterAll
  with Config
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

    "return error when we passed wrong id format" in {
      val id = "this_is_not_long_id"

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
