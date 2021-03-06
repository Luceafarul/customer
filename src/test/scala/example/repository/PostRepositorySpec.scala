package example.repository

import java.time.LocalDateTime

import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import example.config.Config
import example.database.{DatabaseService, FlywayService}
import example.domain.{Customer, Post}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import slick.jdbc.JdbcBackend.Database

class PostRepositorySpec extends AsyncWordSpec
  with Config
  with Matchers
  with BeforeAndAfterAll
  with ForAllTestContainer {

  override val container: PostgreSQLContainer = PostgreSQLContainer(
    dockerImageNameOverride = "postgres:9.6",
    databaseName = "customer_test"
  )
  container.start()

  private val flywayService: FlywayService = new FlywayService(container.jdbcUrl, container.username, container.password)
  private val databaseService: DatabaseService = new DatabaseService {
    override def db: Database = Database.forURL(container.jdbcUrl, container.username, container.password)

    db.createSession()
  }

  private val postRepository = new PostRepository(databaseService)
  private val customerRepository = new CustomerRepository(databaseService)

  private var post = Post("This is test post", LocalDateTime.now, 0L)
  private val customer = Customer("John Doe")

  override protected def beforeAll(): Unit = {
    flywayService.migrateDatabase

    for {
      createdCustomer <- customerRepository.create(customer)
    } yield {
      post = post.copy(customerId = createdCustomer.id.get)
    }
  }

  override protected def afterAll(): Unit =
    flywayService.dropDatabase

  "The Post Repository" should {
    "be empty at the beginning" in {
      postRepository.all.map { posts => posts.size shouldBe 0 }
    }

    "create valid post" in {
      postRepository.create(post).flatMap { createdPost =>
        createdPost.id shouldBe defined
        createdPost.content shouldBe post.content
        createdPost.createdAt shouldBe post.createdAt
        createdPost.customerId shouldBe post.customerId

        postRepository.all.map { posts => posts.size shouldBe 1 }
      }
    }

    "find post by id" in {
      for {
        createdPost <- postRepository.create(post)
        foundPost <- postRepository.findById(createdPost.id.get)
      } yield {
        foundPost shouldBe defined
        foundPost.get shouldBe createdPost
      }
    }

    "find posts by customer id" in {
      for {
        secondCustomer <- customerRepository.create(Customer("John Gold"))
        firstPost <- postRepository.create(post.copy(customerId = secondCustomer.id.get))
        secondPost <- postRepository.create(post.copy(customerId = secondCustomer.id.get))
        foundPosts <- postRepository.allByCustomerId(secondCustomer.id.get)
      } yield {
        foundPosts.size shouldBe 2
        foundPosts should contain only(firstPost, secondPost)
      }
    }

    "find concrete post by customer id and post id" in {
      for {
        secondCustomer <- customerRepository.create(Customer("John Gold"))
        firstPost <- postRepository.create(post.copy(customerId = secondCustomer.id.get))
        secondPost <- postRepository.create(post.copy(customerId = secondCustomer.id.get))
        foundPost <- postRepository.findByCustomerIdAndPost(secondCustomer.id.get, secondPost.id.get)
      } yield {
        foundPost shouldBe defined
        foundPost.get shouldBe secondPost
      }
    }

    "delete customer by id" in {
      for {
        createdPost <- postRepository.create(post)
        _ <- postRepository.delete(createdPost.id.get)
        foundPost <- postRepository.findById(createdPost.id.get)
      } yield {
        foundPost should not be defined
      }
    }
  }
}
