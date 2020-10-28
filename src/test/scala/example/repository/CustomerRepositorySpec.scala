package example.repository

import example.config.Config
import example.database.{FlywayService, PostgresService}
import example.domain.Customer
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class CustomerRepositorySpec extends AsyncWordSpec
  with Config
  with Matchers
  with BeforeAndAfterAll {

  private val flywayService = new FlywayService(dbUrl, dbUser, dbPassword)
  private val databaseService = new PostgresService()
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
