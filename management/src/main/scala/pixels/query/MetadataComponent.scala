package pixels.query

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import pixels.common.db.DbConfiguration._
import pixels.db.model.ImageMetadataDbEntity
import pixels.common.db.TableRepository

object MetadataComponent {

  import dbConfig.profile.api._

  class MetadataTable(tag: Tag) extends Table[ImageMetadataDbEntity](tag, "metadata") {
    def id = column[String]("id", O.PrimaryKey)
    def width = column[Int]("width")
    def height = column[Int]("height")
    def focalLength = column[Int]("focal_length")
    def aperture = column[Double]("aperture")
    def exposure = column[String]("exposure")
    def iso = column[Int]("iso")

    def * =
      (id, width, height, focalLength, aperture, exposure, iso) <> (ImageMetadataDbEntity.tupled, ImageMetadataDbEntity.unapply)
  }

  val metadataTable = TableQuery[MetadataTable]

  class MetadataDbRepository(val config: DatabaseConfig[JdbcProfile])
      extends TableRepository[MetadataTable, String] {
    val table = metadataTable

    def getId(table: MetadataTable) = table.id

    def deleteById(id: String) = db.run(filterById(id).delete)

    def createSchema() = db.run(table.schema.create)
    def dropSchema() = db.run(table.schema.drop)
  }

}
