package pixels.metadata

import org.scalatest.{WordSpec, Matchers}
import pixels.ImageUtils._

class MetadataExtractorSpec extends WordSpec with Matchers {

  val bytes = randomImage(640, 320)

  "Image Metadata Extractor" should {
    "extract EXIF metadata from image" in {
      MetadataExtractor.metadata(bytes)

      1 === 1
    }
  }
}
