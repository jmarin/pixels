package pixels.service
import pixels.repository.ImageRepository
import pixels.model.Image
import scala.concurrent.Future
import pixels.model.ImageData
import scala.concurrent.ExecutionContext
import com.mortennobel.imagescaling.ResampleOp
import com.mortennobel.imagescaling.ResampleFilters
import javax.imageio.ImageIO
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.awt.image.BufferedImage
import java.awt.Color

class ImageServiceImpl(val imageRepository: ImageRepository)(implicit ec: ExecutionContext)
    extends ImageService {

  override def addImage(bytes: Array[Byte]): Future[String] = imageRepository.add(bytes)

  override def get(): Future[Image] = imageRepository.get()

  override def remove(id: String): Future[String] = imageRepository.remove(id)

  override def getImageData: Future[ImageData] =
    imageRepository.get().map(_.data)

  override def monochrome(image: Image): Image = {
    val bufferedImage = ImageIO.read(new ByteArrayInputStream(image.data.bytes))
    val result = new BufferedImage(
      image.metadata.width,
      image.metadata.height,
      BufferedImage.TYPE_INT_RGB
    )
    val graphic = result.createGraphics()
    graphic.drawImage(bufferedImage, 0, 0, Color.WHITE, null)
    for (i <- 0 to result.getHeight() - 1) {
      for (j <- 0 to result.getWidth() - 1) {
        val c = new Color(result.getRGB(j, i))
        val red = (c.getRed() * 0.299).toInt
        val green = (c.getGreen() * 0.587).toInt
        val blue = (c.getBlue() * 0.114).toInt
        val newColor = new Color(
          red + green + blue,
          red + green + blue,
          red + green + blue
        )
        result.setRGB(j, i, newColor.getRGB())
      }
    }
    val outputStream = new ByteArrayOutputStream
    ImageIO.write(result, "jpg", outputStream)
    val bytes = outputStream.toByteArray()
    image.copy(data = ImageData(bytes))
  }

  override def resize(image: Image, width: Int, height: Int): Image = {
    val resizeOp = new ResampleOp(width, height)
    resizeOp.setFilter(ResampleFilters.getLanczos3Filter())
    val bytes = image.data.bytes
    val bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes))
    val scaledBufferedImage = resizeOp.filter(bufferedImage, null)
    val outputStream = new ByteArrayOutputStream
    ImageIO.write(scaledBufferedImage, "jpg", outputStream)
    val scaledBytes = outputStream.toByteArray()
    Image(ImageData(scaledBytes), image.metadata.copy(width = width, height = height))
  }

}
