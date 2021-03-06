package pixels.query

import ImageMetadataDbEntityGenerator._
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import pixels.common.db.DbConfiguration._
import scala.concurrent.Await
import scala.concurrent.duration._
import org.scalatest.AsyncWordSpec
import pixels.query.MetadataComponent.MetadataDbRepository

class MetadataComponentSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  implicit val repository = new MetadataDbRepository(dbConfig)
  val db = repository.db

  val duration = 5.seconds

  override def beforeAll(): Unit = {
    val setup = repository.createSchema()
    Await.result(setup, duration)
  }

  override def afterAll(): Unit = {
    val tearDown = repository.dropSchema()
    Await.result(tearDown, duration)
  }

  "Metadata component" should {
    "be able to insert, read and delete metadata records" in {
      val metadataEntity = imageMetadataDbEntityGenerator.sample.get

      repository.insertOrUpdate(metadataEntity).map(i => i shouldBe i)

      repository.findById(metadataEntity.id).map {
        case Some(m) => {
          m.id shouldBe metadataEntity.id
          m.aperture shouldBe metadataEntity.aperture
        }
        case None => fail("Could not retrieve data")
      }

      repository.deleteById(metadataEntity.id).map(i => i shouldBe 1)

      repository.findById(metadataEntity.id).map {
        case Some(m) => fail("Deletion failed")
        case None    => succeed
      }
    }
  }

}
