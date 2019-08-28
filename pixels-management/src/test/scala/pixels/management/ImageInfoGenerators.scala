package pixels.management

import org.scalacheck._
import pixels.metadata.PixelsMetadata

object ImageInfoGenerators {
  implicit def pixelsMetadataGen: Gen[PixelsMetadata] =
    for {
      s3Url <- Gen.alphaStr
      width <- Gen.choose(1, 20000)
      height <- Gen.choose(1, 20000)
      rating <- Gen.choose(0, 5)
    } yield PixelsMetadata(s3Url, width, height, rating)

}
