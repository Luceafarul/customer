package example.repository

import java.time.LocalDateTime

import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import example.config.Config
import example.database.{DatabaseService, FlywayService}
import example.domain.Post
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

  private val post = Post("This is test post", LocalDateTime.now, 1L)

  override protected def beforeAll(): Unit =
    flywayService.migrateDatabase

  override protected def afterAll(): Unit =
    flywayService.dropDatabase

  "The Post Repository" should {
    "be empty at the beginning" in {
      postRepository.all.map { posts => posts.size shouldBe 0 }
    }

    "create valid post" in {
      postRepository.create(post).map { createdPost =>
        createdPost.id shouldBe defined
        createdPost.content shouldBe post.content
        createdPost.createdAt shouldBe post.createdAt
        createdPost.userId shouldBe post.userId
      }

      postRepository.all.map { posts => posts.size shouldBe 1 }
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
