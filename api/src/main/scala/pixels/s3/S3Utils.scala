package pixels.s3

import akka.stream.scaladsl.{Source, Sink}
import akka.stream.ActorMaterializer
import scala.concurrent.Future
import akka.util.ByteString
import akka.stream.alpakka.s3.MultipartUploadResult
import akka.stream.alpakka.s3.scaladsl.S3

trait S3Utils {

  val s3Client = S3

  def uploadToS3(
      source: Source[ByteString, _],
      bucket: String,
      bucketKey: String
  )(implicit mat: ActorMaterializer): Future[MultipartUploadResult] = {

    val s3Sink: Sink[ByteString, Future[MultipartUploadResult]] =
      s3Client.multipartUpload(bucket, bucketKey)
    source.runWith(s3Sink)
  }
}
