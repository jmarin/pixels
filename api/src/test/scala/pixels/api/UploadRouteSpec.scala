package pixels.api

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{WordSpec, Matchers}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.RouteTestTimeout
import scala.concurrent.duration._
import akka.testkit.TestDuration

class UploadRouteSpec
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with FileUploadUtils
    with UploadRoute {

  implicit val timeout = RouteTestTimeout(5.seconds.dilated)

  val textFile = multipartFile("some text contents", "sample.txt")
  val binaryFile =
    multipartFile(Array.fill(20)((scala.util.Random.nextInt(256) - 128).toByte), "sample.jpg")

  val url = s"/upload/image"

  "Upload Image Service" should {
    "return 400 when trying to upload a file that is not an image" in {
      Post(url, textFile) ~> uploadRoute ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }
    "return 202 when uploading a file with .jpg or .jpeg extension" in {
      Post(url, binaryFile) ~> uploadRoute ~> check {
        status shouldBe StatusCodes.Accepted
      }
    }
  }
}
