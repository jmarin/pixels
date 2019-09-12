package pixels.repository

import scala.language.higherKinds

trait ImageRepository[F[_]] {
  def add()
  def get()
  def remove()
}
