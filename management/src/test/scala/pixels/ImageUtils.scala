package pixels

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import java.awt.Color

object ImageUtils {
  def randomImage: Array[Byte] = {
    val width = 640
    val height = 320
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
}
