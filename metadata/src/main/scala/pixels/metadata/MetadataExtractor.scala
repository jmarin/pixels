package pixels.metadata

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants

object MetadataExtractor {
  def metadata(bytes: Array[Byte]): ImageMetadata = {
    val jpegMetadata = Imaging
      .getMetadata(bytes)
      .asInstanceOf[JpegImageMetadata]

    val aperture =
      jpegMetadata.findEXIFValue(ExifTagConstants.EXIF_TAG_APERTURE_VALUE).getDoubleValue()

    val exposure =
      jpegMetadata
        .findEXIFValue(ExifTagConstants.EXIF_TAG_SHUTTER_SPEED_VALUE)
        .getValueDescription()

    val width =
      jpegMetadata.findEXIFValue(ExifTagConstants.EXIF_TAG_EXIF_IMAGE_WIDTH).getIntValue()

    val height =
      jpegMetadata.findEXIFValue(ExifTagConstants.EXIF_TAG_EXIF_IMAGE_LENGTH).getIntValue()

    val iso =
      jpegMetadata.findEXIFValue(ExifTagConstants.EXIF_TAG_ISO).getIntValue()

    val focalLength =
      jpegMetadata.findEXIFValue(ExifTagConstants.EXIF_TAG_FOCAL_LENGTH).getIntValue()

    ImageMetadata(
      width = width,
      height = height,
      focalLength = focalLength,
      aperture = aperture,
      exposure = exposure,
      ISO = iso
    )
  }
}
