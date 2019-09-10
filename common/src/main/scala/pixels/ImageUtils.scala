package pixels

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import java.awt.Color
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants
import org.apache.commons.imaging.common.RationalNumber

object ImageUtils {

  val exifRewriter = new ExifRewriter

  def randomImage(width: Int, height: Int): Array[Byte] = {
    val img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

    for (y <- 0 to height - 1) {
      for (x <- 0 to width - 1) {
        val r: Int = (Math.random() * 256).toInt
        val g: Int = (Math.random() * 256).toInt
        val b: Int = (Math.random() * 256).toInt

        val p = new Color(r, g, b).getRGB()
        img.setRGB(x, y, p)
      }

    }
    val baos = new ByteArrayOutputStream()
    ImageIO.write(img, "jpg", baos)
    baos.flush()
    baos.toByteArray()
  }

  def randomImageWithMetadata(width: Int, height: Int): Array[Byte] = {
    val bytes = randomImage(width, height)
    val tiffOutputSet = new TiffOutputSet

    val os = new ByteArrayOutputStream
    val exifDirectory = tiffOutputSet.getOrCreateExifDirectory()

    exifDirectory.removeField(ExifTagConstants.EXIF_TAG_APERTURE_VALUE);
    exifDirectory.add(ExifTagConstants.EXIF_TAG_APERTURE_VALUE, new RationalNumber(10, 4))

    exifDirectory.removeField(ExifTagConstants.EXIF_TAG_EXIF_IMAGE_WIDTH)
    exifDirectory.add(ExifTagConstants.EXIF_TAG_EXIF_IMAGE_WIDTH, width.toShort)

    exifDirectory.removeField(ExifTagConstants.EXIF_TAG_EXIF_IMAGE_LENGTH)
    exifDirectory.add(ExifTagConstants.EXIF_TAG_EXIF_IMAGE_LENGTH, height.toShort)

    exifDirectory.removeField(ExifTagConstants.EXIF_TAG_SHUTTER_SPEED_VALUE)
    exifDirectory.add(ExifTagConstants.EXIF_TAG_SHUTTER_SPEED_VALUE, new RationalNumber(1, 125))

    exifDirectory.removeField(ExifTagConstants.EXIF_TAG_ISO)
    exifDirectory.add(ExifTagConstants.EXIF_TAG_ISO, 100.toShort)

    exifDirectory.removeField(ExifTagConstants.EXIF_TAG_FOCAL_LENGTH)
    exifDirectory.add(ExifTagConstants.EXIF_TAG_FOCAL_LENGTH, new RationalNumber(110, 1))

    exifDirectory.removeField(ExifTagConstants.EXIF_TAG_FOCAL_LENGTH_IN_35MM_FORMAT)
    exifDirectory.add(
      ExifTagConstants.EXIF_TAG_FOCAL_LENGTH_IN_35MM_FORMAT,
      87.toShort
    )

    new ExifRewriter().updateExifMetadataLossless(bytes, os, tiffOutputSet)
    os.flush()
    os.toByteArray()
  }

}
