package example.repository

import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import example.config.Config
import example.database.{DatabaseService, FlywayService}
import example.domain.Customer
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import slick.jdbc.JdbcBackend.Database

class CustomerRepositorySpec extends AsyncWordSpec
  with Config
  with Matchers
  with BeforeAndAfterAll
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

  override protected def beforeAll(): Unit =
    flywayService.migrateDatabase

  override protected def afterAll(): Unit =
    flywayService.dropDatabase

  "The Customer Repository" should {
    "be empty at the beginning" in {
      customerRepository.all.map { customers => customers.size shouldBe 0 }
    }

    "create valid customer" in {
      val customer = Customer("John Doe")

      customerRepository.create(customer).flatMap { createdCustomer =>
        createdCustomer.id shouldBe defined
        createdCustomer.name shouldBe "John Doe"
      }
      customerRepository.all.map { customers => customers.size shouldBe 1 }
    }

    "find customer by id" in {
      val customer = Customer("John Doe")

      for {
        createdCustomer <- customerRepository.create(customer)
        foundCustomer <- customerRepository.findById(createdCustomer.id.get)
      } yield {
        foundCustomer shouldBe defined
        foundCustomer.get shouldBe createdCustomer
      }
    }

    "delete customer by id" in {
      val customer = Customer("John Doe")

      for {
        createdCustomer <- customerRepository.create(customer)
        _ <- customerRepository.delete(createdCustomer.id.get)
        foundCustomer <- customerRepository.findById(createdCustomer.id.get)
      } yield foundCustomer should not be defined
    }
  }
}
