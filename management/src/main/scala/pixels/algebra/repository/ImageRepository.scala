package pixels.algebra.repository

import scala.language.higherKinds
import pixels.model.Image
import cats.data.OptionT

trait ImageRepository[F[_]] {
  def add(bytes: Array[Byte]): F[String]
  def get(): F[Image]
  def remove(id: String): F[String]
}
