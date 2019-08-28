package pixels.metadata

import org.apache.commons.imaging.Imaging

trait ImageMetadataExtractor {

  def extractMetadata(s3Url: String): Unit = {
    //val exif = Imaging.getMetadata(bytes)

  }

}
