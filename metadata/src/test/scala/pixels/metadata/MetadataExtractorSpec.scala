package pixels.metadata

import org.scalatest.{WordSpec, Matchers}
import pixels.ImageUtils._

class MetadataExtractorSpec extends WordSpec with Matchers {

  val bytes = randomImageWithMetadata(640, 320)

  "Image Metadata Extractor" should {
    "extract EXIF metadata from image" in {
      val metadata: ImageMetadata = MetadataExtractor.metadata(bytes)
      metadata.width shouldBe 640
      metadata.height shouldBe 320
      metadata.focalLength shouldBe 110
      metadata.ISO shouldBe 100
      metadata.aperture shouldBe 2.5
      metadata.exposure shouldBe "1/125 (0.008)"
    }
  }
}
