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

  "Upload Image Service" should {
    "return 400 when trying to upload a file that is not an image" in {
      Post("/upload", textFile) ~> uploadImageRoute ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }
  }
}
