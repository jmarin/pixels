package pixels.query

import ImageMetadataDbEntityGenerator._
import org.scalatest.AsyncWordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import org.scalatest.PropSpec
import org.scalatest.prop.PropertyChecks
import slick.dbio.DBIOAction
import pixels.common.db.DbConfiguration._
import scala.concurrent.Await
import scala.concurrent.duration._
import org.scalatest.AsyncWordSpec

class MetadataComponentSpec
    extends AsyncWordSpec
    with Matchers
    with BeforeAndAfterAll
    with MetadataComponent {

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
    }
  }

}
