package pixels.persistence

import org.scalatest.Matchers
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.Cluster
import akka.cluster.typed.Join
import pixels.model.ImageData
import org.scalacheck.Gen
import pixels.persistence.ImageEntity
import pixels.persistence.ImageEntity.AddImage
import akka.Done
import akka.actor.testkit.typed.scaladsl.TestProbe
import pixels.persistence.ImageEntity.GetImage
import pixels.persistence.ImageEntity.RemoveImage
import pixels.ImageUtils._
import org.scalatest.WordSpecLike

class ImageEntitySpec extends ScalaTestWithActorTestKit with WordSpecLike with Matchers {

  val sharding = ClusterSharding(system)
  ImageEntity.startShardRegion(sharding)

  val id = Gen.sample.getOrElse("image1").toString()
  val bytes = randomImage(640, 320)

  val sampleImage = ImageData(bytes)

  val imageDoneProbe = TestProbe[Done]("image-done-probe")
  val imageProbe = TestProbe[Option[ImageData]]("image-probe")

  "Image Entity" should {
    Cluster(system).manager ! Join(Cluster(system).selfMember.address)
    "persist images" in {
      val imageEntity = sharding.entityRefFor(ImageEntity.TypeKey, s"${ImageEntity.name}-$id")

      imageEntity ! AddImage(id, bytes, imageDoneProbe.ref)
      imageDoneProbe.expectMessage(Done)
    }

    "retrieve image" in {
      val imageEntity = sharding.entityRefFor(ImageEntity.TypeKey, s"${ImageEntity.name}-$id")

      imageEntity ! GetImage(imageProbe.ref)
      imageProbe.expectMessage(Some(sampleImage))
    }

    "remove image" in {
      val imageEntity = sharding.entityRefFor(ImageEntity.TypeKey, s"${ImageEntity.name}-$id")

      imageEntity ! RemoveImage(imageDoneProbe.ref)
      imageDoneProbe.expectMessage(Done)

      imageEntity ! GetImage(imageProbe.ref)
      imageProbe.expectMessage(None)
    }
  }

}
