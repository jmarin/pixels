package pixels.metadata

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata

object MetadataExtractor {
  def metadata(bytes: Array[Byte]): ImageMetadata = {
    println(
      Imaging
        .getMetadata(bytes)
        .getItems()
        .size()
    )
    //   .asInstanceOf[JpegImageMetadata]
    //   .getItems()
    //   .forEach(x => println(x))

    ImageMetadata(0, 0, 0)
  }
}
