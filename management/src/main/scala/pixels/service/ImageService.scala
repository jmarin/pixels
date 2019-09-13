package pixels.service

import scala.concurrent.Future
import pixels.model.{Image, ImageData}
import pixels.repository.ImageRepository

trait ImageService {
  val imageRepository: ImageRepository
  def addImage(bytes: Array[Byte]): Future[String]
  def get: Future[Image]
  def getImageData: Future[ImageData]
  def remove(id: String): Future[String]
  def resize(image: Image, width: Int, height: Int): Image
  def monochrome(image: Image): Image
}
