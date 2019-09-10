package pixels.api

import org.scalatest.WordSpec
import org.scalatest.Matchers
import akka.http.scaladsl.testkit.ScalatestRouteTest
import pixels.common.api.FileUploadUtils
import akka.http.scaladsl.testkit.RouteTestTimeout
import scala.concurrent.duration._
import akka.testkit.TestDuration
import akka.cluster.typed.Cluster
import akka.cluster.typed.Join
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.util.Timeout
import akka.http.scaladsl.model.StatusCodes
import pixels.ImageUtils._
import pixels.persistence.ImageEntity
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.MediaTypes

class ImageRouteSpec
    extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with FileUploadUtils
    with ImageRoute {

  val timeoutDuration = 5.seconds

  override val sharding: ClusterSharding = ClusterSharding(system.toTyped)
  override val timeout: Timeout = timeoutDuration
  implicit val untypedSystem = system
  implicit val typedSystem = system.toTyped
  implicit val routeTimeout = RouteTestTimeout(timeoutDuration.dilated)

  ImageEntity.startShardRegion(sharding)

  val url = "/images"

  val textFile = multipartFile("some text contents", "sample.txt")
  val imageFile = multipartFile(randomImage, "sample.jpg")

  "Image Managerment Service" should {
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)

    "Return Bad Request when trying to upload a file that is not an image" in {
      Post(url, textFile) ~> uploadRoute ~> check {
        status shouldBe StatusCodes.BadRequest
      }
    }

    "Successfully upload an image in .jpg format" in {
      Post(url, imageFile) ~> uploadRoute ~> check {
        status shouldBe StatusCodes.Created
      }
      Get(s"$url/sample.jpg") ~> imageRoute ~> check {
        status shouldBe StatusCodes.OK
        response.entity.contentType.mediaType shouldBe MediaTypes.`image/jpeg`
      }
    }

    "Successfully delete an image" in {
      Delete(s"$url/sample.jpg") ~> imageRoute ~> check {
        status shouldBe StatusCodes.NoContent
      }
      Get(s"$url/sample.jpg") ~> imageRoute ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }
  }

}
