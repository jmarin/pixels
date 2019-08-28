package pixels.management

import akka.Done
import org.scalatest.WordSpec
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import ImageInfoGenerators._
import pixels.metadata.PixelsMetadata
import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.cluster.typed.Cluster
import akka.cluster.typed.Join
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import pixels.management.ImageMetadataEntity.{AddImageMetadata, GetImageMetadata}
import pixels.management.ImageMetadataEntity.RateImage
import pixels.management.ImageMetadataEntity.RemoveImageMetadata

class ImageMetadataEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  protected val testkit: ActorTestKit = ActorTestKit("ImageInfoEntityTestkit")

  import testkit._

  val pixelMetadata1 = pixelsMetadataGen.sample.getOrElse(PixelsMetadata())

  val imageDoneProbe = TestProbe[Done]("image-info-probe")
  val imageMetadataProbe = TestProbe[Option[PixelsMetadata]]

  val sharding = ClusterSharding(system)
  ImageMetadataEntity.startShardRegion(sharding)

  override def afterAll(): Unit = {
    shutdownTestKit()
    super.afterAll()
  }

  "Image Metadata" should {
    Cluster(system).manager ! Join(Cluster(system).selfMember.address)
    "persist" in {
      val imageMetadata =
        sharding.entityRefFor(ImageMetadataEntity.TypeKey, s"${ImageMetadataEntity.name}-ABC12345")

      imageMetadata ! AddImageMetadata(pixelMetadata1, imageDoneProbe.ref)
      imageDoneProbe.expectMessage(Done)

      imageMetadata ! GetImageMetadata(imageMetadataProbe.ref)
      imageMetadataProbe.expectMessage(Some(pixelMetadata1))

    }

    "update rating" in {
      val imageMetadata =
        sharding.entityRefFor(ImageMetadataEntity.TypeKey, s"${ImageMetadataEntity.name}-ABC12345")

      imageMetadata ! RateImage(3, imageDoneProbe.ref)
      imageDoneProbe.expectMessage(Done)

      imageMetadata ! GetImageMetadata(imageMetadataProbe.ref)
      imageMetadataProbe.expectMessage(Some(pixelMetadata1.copy(rating = 3)))

    }

    "delete" in {
      val imageMetadata =
        sharding.entityRefFor(ImageMetadataEntity.TypeKey, s"${ImageMetadataEntity.name}-ABC12345")

      imageMetadata ! RemoveImageMetadata(imageDoneProbe.ref)
      imageDoneProbe.expectMessage(Done)

      imageMetadata ! GetImageMetadata(imageMetadataProbe.ref)
      imageMetadataProbe.expectMessage(None)

    }
  }

}
