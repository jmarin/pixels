package pixels.s3

import akka.stream.scaladsl.{Source, Sink}
import akka.stream.ActorMaterializer
import scala.concurrent.Future
import akka.util.ByteString
import akka.stream.alpakka.s3.MultipartUploadResult
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.alpakka.s3.ObjectMetadata
import akka.NotUsed
import akka.http.scaladsl.model.ContentType
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.MediaTypes

trait S3Utils {

  val s3Client = S3

  def uploadToS3(
      source: Source[ByteString, _],
      bucket: String,
      bucketKey: String,
      contentType: ContentType
  )(implicit mat: ActorMaterializer): Future[MultipartUploadResult] = {

    val s3Sink: Sink[ByteString, Future[MultipartUploadResult]] =
      s3Client.multipartUpload(bucket, bucketKey, contentType)
    source.runWith(s3Sink)
  }

  def imageBytesFromS3(bucket: String, bucketKey: String)(implicit mat: ActorMaterializer) = {
    val s3File: Source[Option[(Source[ByteString, NotUsed], ObjectMetadata)], NotUsed] =
      s3Client
        .download(bucket, bucketKey)
        .map {
          case Some((source, metadata)) =>
            val contentType = metadata.contentType
              .flatMap(ContentType.parse(_).right.toOption)
              .getOrElse(ContentTypes.`application/octet-stream`)
            if (contentType == MediaTypes.`image/jpeg`) {}
        }
  }

}
