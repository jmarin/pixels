package pixels.api

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{WordSpec, Matchers}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.RouteTestTimeout
import scala.concurrent.duration._
import akka.testkit.TestDuration
import org.scalatest.BeforeAndAfterAll
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.actor.typed.scaladsl.adapter._

class UploadRouteSpec
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with FileUploadUtils
    with ImageRoute
    with BeforeAndAfterAll {

  implicit val timeout = RouteTestTimeout(5.seconds.dilated)

  val textFile = multipartFile("some text contents", "sample.txt")
  val binaryFile =
    multipartFile(Array.fill(20)((scala.util.Random.nextInt(256) - 128).toByte), "sample.jpg")

  val url = s"/images"

  override def beforeAll(): Unit = {}

  override def afterAll(): Unit = {}

  override val sharding: ClusterSharding = ClusterSharding(system.toTyped)

  "Upload Image Service" should {
    "return 400 when trying to upload a file that is not an image" in {
      Post(url, textFile) ~> uploadRoute ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }
    //TODO: test with local instance of S3
    //"return 202 when uploading a file with .jpg or .jpeg extension" in {
    //  Post(url, binaryFile) ~> uploadRoute ~> check {
    //    status shouldBe StatusCodes.Accepted
    //  }
    //}
  }

}
