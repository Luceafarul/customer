package example.service

import example.domain.Customer
import example.repository.CustomerRepository
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.Future

class CustomerServiceSpec extends AnyWordSpec with Matchers with MockFactory {
  val customerRepositoryStub: CustomerRepository = stub[CustomerRepository]
  val customerService = new CustomerService(customerRepositoryStub)

  "CustomerService" should {
    "create customer" in {
      val customer = Customer("Marc Aurelius")
      val createdCustomer = Future.successful(Customer("Marc Aurelius", Some(1L)))

      (customerRepositoryStub.create _).when(customer).returns(createdCustomer)

      customerService.create(customer) shouldBe createdCustomer
    }

    "return customer if it exist" in {
      val existedCustomer = Future.successful(Option(Customer("Marc Aurelius", Some(1L))))

      (customerRepositoryStub.findById _).when(1L).returns(existedCustomer)

      customerService.get(id = 1L) shouldBe existedCustomer
    }

    "return None if customer does not exist" in {
      val notExistResponse = Future.successful(None)

      (customerRepositoryStub.findById _).when(*).returns(notExistResponse)

      customerService.get(id = 777) shouldBe notExistResponse
    }

    "delete customer by id" in {
      val customerId = 1L

      (customerRepositoryStub.delete _)
        .when(customerId)
        .returns(Future.successful(1))

      customerService.delete(customerId)
    }
  }
}
