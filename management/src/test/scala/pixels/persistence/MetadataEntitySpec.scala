package pixels.persistence

import akka.Done
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.{WordSpecLike, Matchers}
import pixels.ImageUtils._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import pixels.metadata.MetadataExtractor._
import akka.cluster.typed.Cluster
import akka.cluster.typed.Join
import akka.actor.testkit.typed.scaladsl.TestProbe
import pixels.metadata.ImageMetadata
import org.scalacheck.Gen
import pixels.persistence.MetadataEntity.AddMetadata
import pixels.persistence.MetadataEntity.GetMetadata
import pixels.persistence.MetadataEntity.RemoveMetadata

class MetadataEntitySpec extends ScalaTestWithActorTestKit with WordSpecLike with Matchers {

  val sharding = ClusterSharding(system)
  MetadataEntity.startShardRegion(sharding)

  val id = Gen.sample.getOrElse("image1").toString()
  val randomImage = randomImageWithMetadata(640, 320)
  val imageMetadata = metadata(randomImage)

  val metadataDoneProbe = TestProbe[Done]
  val metadataProbe = TestProbe[Option[ImageMetadata]]

  "Metadata Entity" should {
    Cluster(system).manager ! Join(Cluster(system).selfMember.address)
    "persist metadata" in {
      val metadataEntity =
        sharding.entityRefFor(MetadataEntity.TypeKey, s"${MetadataEntity.name}-$id")

      metadataEntity ! AddMetadata(imageMetadata, metadataDoneProbe.ref)
      metadataDoneProbe.expectMessage(Done)
    }

    "retrieve metadata" in {
      val metadataEntity =
        sharding.entityRefFor(MetadataEntity.TypeKey, s"${MetadataEntity.name}-$id")

      metadataEntity ! GetMetadata(metadataProbe.ref)
      metadataProbe.expectMessage(Some(imageMetadata))
    }

    "remove metadata" in {
      val metadataEntity =
        sharding.entityRefFor(MetadataEntity.TypeKey, s"${MetadataEntity.name}-$id")

      metadataEntity ! RemoveMetadata(metadataDoneProbe.ref)
      metadataDoneProbe.expectMessage(Done)

      metadataEntity ! GetMetadata(metadataProbe.ref)
      metadataProbe.expectMessage(None)
    }
  }

}
