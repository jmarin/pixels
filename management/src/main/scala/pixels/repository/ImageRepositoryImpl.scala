package pixels.repository

import akka.Done
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import pixels.persistence.ImageEntity
import pixels.persistence.MetadataEntity
import scala.concurrent.Future
import pixels.model.Image
import scala.concurrent.duration._
import akka.util.Timeout
import scala.concurrent.ExecutionContext
import pixels.metadata.MetadataExtractor._
import com.typesafe.config.ConfigFactory
import pixels.persistence.MetadataEntity.GetMetadata
import pixels.model.ImageData
import pixels.metadata.ImageMetadata

class ImageRepositoryImpl(id: String, sharding: ClusterSharding)(
    implicit ec: ExecutionContext
) extends ImageRepository {

  val config = ConfigFactory.load()
  val futureTimeout = config.getInt("pixels.future.timeout")

  implicit val timeout = Timeout(futureTimeout.seconds)

  val imageEntity = sharding.entityRefFor(ImageEntity.TypeKey, s"${ImageEntity.name}-$id")
  val metadataEntity = sharding.entityRefFor(MetadataEntity.TypeKey, s"${MetadataEntity.name}-$id")

  override def add(bytes: Array[Byte]): Future[String] = {
    val imageMetadata = metadata(bytes)

    val dataF: Future[Done] = imageEntity ? (ref => ImageEntity.AddImage(id, bytes, ref))
    val metadataF: Future[Done] = metadataEntity ? (
        ref => MetadataEntity.AddMetadata(imageMetadata, ref)
    )

    for {
      data <- dataF
      metadata <- metadataF
    } yield id

  }

  override def get(): Future[Image] = {

    val dataF: Future[ImageData] =
      imageEntity ? (ref => ImageEntity.GetImage(ref)) map (
          x => x.getOrElse(ImageData(Array.empty[Byte]))
      )

    val metadataF: Future[ImageMetadata] = metadataEntity ? (ref => GetMetadata(ref)) map (
        x => x.getOrElse(ImageMetadata())
    )

    for {
      data <- dataF
      metadata <- metadataF
      image = Image(data, metadata)
    } yield image
  }

  override def remove(id: String): Future[String] = {
    val dataF: Future[Done] = imageEntity ? (ref => ImageEntity.RemoveImage(ref))
    val metadataF: Future[Done] = metadataEntity ? (ref => MetadataEntity.RemoveMetadata(ref))

    for {
      data <- dataF
      metadata <- metadataF
    } yield id
  }

}
