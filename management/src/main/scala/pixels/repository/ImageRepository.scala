package pixels.repository

import pixels.model.Image
import scala.concurrent.Future

trait ImageRepository {
  def add(bytes: Array[Byte]): Future[String]
  def get(): Future[Image]
  def remove(id: String): Future[String]
}
