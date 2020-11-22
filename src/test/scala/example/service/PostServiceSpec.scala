package example.service

import java.time.LocalDateTime

import example.domain.Post
import example.repository.PostRepository
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.Future

class PostServiceSpec extends AnyWordSpec with Matchers with MockFactory {
  private val postRepositoryStub: PostRepository = stub[PostRepository]
  private val postService = new PostService(postRepositoryStub)

  private val post = Post("This is test post", LocalDateTime.now, 1L)
  private val createdPost = Future.successful {
    Post("This is test post", LocalDateTime.now, 1L, Some(1L))
  }

  "The Post Service" should {
    "create post" in {
      (postRepositoryStub.create _).when(post).returns(createdPost)

      postService.create(post) shouldBe createdPost
    }

    "return post if it exist" in {
      val existedPost = Future.successful(
        Option(
          Post("This is test post", LocalDateTime.now, 1L, Some(1L))
        )
      )

      (postRepositoryStub.findByCustomerIdAndPost _).when(1L, 1L).returns(existedPost)

      postService.getByCustomerIdAndPostId(customerId =1L, postId = 1L) shouldBe existedPost
    }

    "return None if post does not exist" in {
      val notExistResponse = Future.successful(None)

      (postRepositoryStub.findByCustomerIdAndPost _).when(*, *).returns(notExistResponse)

      postService.getByCustomerIdAndPostId(customerId = 1, postId = 777) shouldBe notExistResponse
    }

    "return all posts by customer id" in {
      val customerId = 2L
      val secondPost = Post("This is second test post", LocalDateTime.now, customerId)
      val thirdPost = Post("This is third test post", LocalDateTime.now, customerId)

      val searchAllByCustomerId = Future.successful(List(secondPost, thirdPost))

      (postRepositoryStub.allByCustomerId _)
        .when(customerId)
        .returns(searchAllByCustomerId)

      postService.getAllByCustomerId(customerId)
    }

    "delete post by id" in {
      val postId = 1L

      (postRepositoryStub.delete _)
        .when(postId)
        .returns(Future.successful(1))

      postService.delete(postId)
    }
  }
}
