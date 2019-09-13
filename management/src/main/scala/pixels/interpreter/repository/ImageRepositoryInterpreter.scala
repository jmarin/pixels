package pixels.interpreter.repository

import akka.Done
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import pixels.persistence.ImageEntity
import pixels.persistence.MetadataEntity
import pixels.algebra.repository.ImageRepository
import scala.concurrent.Future
import cats.data.OptionT
import pixels.model.Image
import scala.concurrent.duration._
import akka.util.Timeout
import scala.concurrent.ExecutionContext
import pixels.metadata.MetadataExtractor._
import com.typesafe.config.ConfigFactory
import scala.language.higherKinds
import cats._
import cats.data._
import cats.implicits._
import pixels.persistence.MetadataEntity.GetMetadata
import pixels.model.ImageData
import pixels.metadata.ImageMetadata
import cats.data.OptionT

class ImageRepositoryInterpreter[F[_]: Monad](id: String, sharding: ClusterSharding)(
    implicit ec: ExecutionContext
) extends ImageRepository[F] {

  val config = ConfigFactory.load()
  val futureTimeout = config.getInt("pixels.future.timeout")

  implicit val timeout = Timeout(futureTimeout.seconds)

  val imageEntity = sharding.entityRefFor(ImageEntity.TypeKey, s"${ImageEntity.name}-$id")
  val metadataEntity = sharding.entityRefFor(MetadataEntity.TypeKey, s"${MetadataEntity.name}-$id")

  override def add(bytes: Array[Byte]): F[String] = {
    val imageMetadata = metadata(bytes)

    val dataF = imageEntity ? (ref => ImageEntity.AddImage(id, bytes, ref))
    val metadataF = metadataEntity ? (
        ref => MetadataEntity.AddMetadata(imageMetadata, ref)
    )

    for {
      data <- dataF.pure[F]
      metadata <- metadataF.pure[F]
    } yield id

  }

  override def get(): F[Image] = {
    val dataF: Future[Option[ImageData]] = imageEntity ? (ref => ImageEntity.GetImage(ref))
    val metadataF: Future[Option[ImageMetadata]] = metadataEntity ? (ref => GetMetadata(ref))

    // val i = for {
    //   d <- dataF.pure[F]
    //   m <- metadataF.pure[F]
    // } yield Image(d, m)

    ???
  }

  override def remove(id: String): F[String] = ???

}
