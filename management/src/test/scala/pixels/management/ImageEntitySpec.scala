package pixels.management
import org.scalatest.WordSpec
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.Cluster
import akka.cluster.typed.Join
import pixels.persistence.ImageEntity.Image
import org.scalacheck.Gen
import pixels.persistence.ImageEntity
import pixels.persistence.ImageEntity.AddImage
import akka.Done
import akka.actor.testkit.typed.scaladsl.TestProbe
import pixels.persistence.ImageEntity.GetImage
import pixels.persistence.ImageEntity.RemoveImage
import pixels.ImageUtils._

class ImageEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  protected val testkit = ActorTestKit("ImageEntityTestKit")

  import testkit._

  val sharding = ClusterSharding(system)
  ImageEntity.startShardRegion(sharding)

  override def afterAll(): Unit = {
    shutdownTestKit()
    super.afterAll()
  }

  val id = Gen.sample.getOrElse("image1").toString()
  val bytes = randomImage

  val sampleImage = Image(id, bytes)

  val imageDoneProbe = TestProbe[Done]("image-done-probe")
  val imageProbe = TestProbe[Option[Image]]("image-probe")

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
