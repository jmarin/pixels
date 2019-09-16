package pixels.query

import org.scalacheck.Gen
import pixels.db.model.ImageMetadataDbEntity

object ImageMetadataDbEntityGenerator {
  implicit def imageMetadataDbEntityGenerator: Gen[ImageMetadataDbEntity] = {
    for {
      id <- Gen.alphaStr
      width <- Gen.choose(Int.MinValue, Int.MaxValue)
      height <- Gen.choose(Int.MinValue, Int.MaxValue)
      focalLength <- Gen.choose(12, 600)
      aperture <- Gen.oneOf(1.4, 2.0, 2.8, 4.0, 5.6, 8.0, 11.0)
      exposure <- Gen.oneOf("1/125", "1/250", "1/500")
      iso <- Gen.oneOf(100, 200, 400, 800, 1600, 3200)
    } yield ImageMetadataDbEntity(id, width, height, focalLength, aperture, exposure, iso)
  }
}
