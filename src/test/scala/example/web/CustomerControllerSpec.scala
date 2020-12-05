package example.web

import java.time.LocalDateTime

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import example.config.Config
import example.database.{DatabaseService, FlywayService}
import example.domain.{Customer, Post}
import example.repository.{CustomerRepository, PostRepository}
import example.service.{CustomerService, PostService}
import io.circe.generic.auto._
import io.circe.syntax._
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
  with ForAllTestContainer
  with FailFastCirceSupport {

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
  private val postRepository = new PostRepository(databaseService)
  private val customerService = new CustomerService(customerRepository)
  private val postService = new PostService(postRepository)
  private val customerController = new CustomerController(customerService, postService)
  private val route = customerController.route

  private val customerServiceStub = stub[CustomerService]
  private val postServiceStub = stub[PostService]
  private val customerControllerWithStub = new CustomerController(customerServiceStub, postServiceStub)
  private val routeWithStub = customerControllerWithStub.route

  private val customer = Customer("Marcus Aurelius")
  private val existedCustomer = Customer("Marcus Aurelius", Some(1L))
  private val existedCustomerId = existedCustomer.id.get

  private val post = example.domain.Post("This is test post", LocalDateTime.now)
  private val existedPost = example.domain.Post("This is test post", LocalDateTime.now, 1L, Some(1L))
  private val existedPostId = existedPost.id.get

  override protected def beforeAll(): Unit =
    flywayService.migrateDatabase

  override protected def afterAll(): Unit =
    flywayService.dropDatabase

  "The Customer Controller" should {
    "return NotFound if customer does not exist" in {
      val notExistedId = 777

      Get(s"/api/customers/$notExistedId") ~> route ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    "return customer by id if existing" in {
      val createdCustomer = Post("/api/customers", customer) ~> route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Customer]
      }

      Get(s"/api/customers/${createdCustomer.id.get}") ~> route ~> check {
        status shouldBe StatusCodes.OK

        val foundCustomer = responseAs[Customer]
        foundCustomer shouldBe createdCustomer
      }
    }

    "return created customer" in {
      Post("/api/customers", customer) ~> route ~> check {
        status shouldBe StatusCodes.OK

        val created = responseAs[Customer]
        created.name shouldBe customer.name
        created.id shouldBe defined
      }
    }

    "deleted existed customer by id" in {
      val createdCustomer = Post("/api/customers", customer) ~> route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Customer]
      }

      Delete(s"/api/customers/${createdCustomer.id.get}") ~> route ~> check {
        status shouldBe StatusCodes.NoContent
      }

      Get(s"/api/customers/${createdCustomer.id.get}") ~> route ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    "return NotFound if post does not exist" in {
      val notExistedPostId = 777

      Get(s"/api/customers/$existedCustomerId/posts/$notExistedPostId") ~> route ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    "return created post" in {
      val createdCustomer = Post("/api/customers", customer) ~> route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Customer]
      }

      Post(s"/api/customers/${createdCustomer.id.get}/posts", post) ~> route ~> check {
        status shouldBe StatusCodes.OK

        val created = responseAs[Post]
        created.content shouldBe post.content
        created.createdAt shouldBe post.createdAt
        created.customerId shouldBe createdCustomer.id.get
        created.id shouldBe defined
      }
    }

    "return post by id if existing" in {
      val createdCustomer = Post("/api/customers", customer) ~> route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Customer]
      }

      val createdPost = Post(s"/api/customers/${createdCustomer.id.get}/posts", post) ~> route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Post]
      }

      Get(s"/api/customers/${createdCustomer.id.get}/posts/${createdPost.id.get}") ~> route ~> check {
        status shouldBe StatusCodes.OK

        val foundPost = responseAs[Post]
        foundPost shouldBe createdPost
      }
    }

    "return all customer's post by customer id" in {
      val createdCustomer = Post("/api/customers", customer) ~> route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Customer]
      }

      val firstPost = Post(s"/api/customers/${createdCustomer.id.get}/posts", post) ~> route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Post]
      }

      val secondPost = Post(s"/api/customers/${createdCustomer.id.get}/posts", post) ~> route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Post]
      }

      Get(s"/api/customers/${createdCustomer.id.get}/posts") ~> route ~> check {
        status shouldBe StatusCodes.OK

        val foundPosts = responseAs[Seq[Post]]
        foundPosts should contain only(firstPost, secondPost)
      }
    }

    "deleted existed post by id" in {
      val createdCustomer = Post("/api/customers", customer) ~> route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Customer]
      }

      val createdPost = Post(s"/api/customers/${createdCustomer.id.get}/posts", post) ~> route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Post]
      }

      Delete(s"/api/customers/posts/${createdPost.id.get}") ~> route ~> check {
        status shouldBe StatusCodes.NoContent
      }

      Get(s"/api/customers/${createdCustomer.id.get}/posts/${createdPost.id.get}") ~> route ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    "return BadRequest when failed retrieve customer" in {
      (customerServiceStub.get _)
        .when(existedCustomerId)
        .returns(Future.failed(new UnsupportedOperationException))

      Get(s"/api/customers/$existedCustomerId") ~> Route.seal(routeWithStub) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "return InternalServerError when failed retrieve customer" in {
      (customerServiceStub.get _)
        .when(existedCustomerId)
        .returns(Future.failed(new OutOfMemoryError("Exception for tests")))

      Get(s"/api/customers/$existedCustomerId") ~> Route.seal(routeWithStub) ~> check {
        status shouldBe StatusCodes.InternalServerError
      }
    }

    "return BadRequest when failed create customer" in {
      (customerServiceStub.create _)
        .when(customer)
        .returns(Future.failed(new UnsupportedOperationException))

      Post("/api/customers", customer) ~> Route.seal(routeWithStub) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "return InternalServerError when failed to create customer" in {
      (customerServiceStub.create _)
        .when(customer)
        .returns(Future.failed(new OutOfMemoryError("Exception for tests")))

      Post("/api/customers", customer) ~> Route.seal(routeWithStub) ~> check {
        status shouldBe StatusCodes.InternalServerError
      }
    }

    "return BadRequest when failed to delete customer" in {
      (customerServiceStub.delete _)
        .when(existedCustomerId)
        .returns(Future.failed(new UnsupportedOperationException))

      Delete(s"/api/customers/$existedCustomerId") ~> Route.seal(routeWithStub) ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "return InternalServerError when failed to delete customer" in {
      (customerServiceStub.delete _)
        .when(existedCustomerId)
        .returns(Future.failed(new OutOfMemoryError("Exception for tests")))

      Delete(s"/api/customers/$existedCustomerId") ~> Route.seal(routeWithStub) ~> check {
        status shouldBe StatusCodes.InternalServerError
      }
    }
  }

  "return BadRequest when failed retrieve post" in {
    (postServiceStub.getByCustomerIdAndPostId _)
      .when(existedCustomerId, existedPostId)
      .returns(Future.failed(new UnsupportedOperationException))

    Get(s"/api/customers/$existedCustomerId/posts/$existedPostId") ~> Route.seal(routeWithStub) ~> check {
      status shouldBe StatusCodes.BadRequest
    }
  }
}
