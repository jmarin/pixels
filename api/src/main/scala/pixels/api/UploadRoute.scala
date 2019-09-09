package pixels.api

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.stream.scaladsl.{Source, Sink}
import akka.stream.ActorMaterializer
import scala.util.{Success, Failure}
import akka.stream.alpakka.s3.scaladsl.S3
import scala.concurrent.Future
import akka.util.ByteString
import akka.stream.alpakka.s3.MultipartUploadResult

trait UploadRoute {

  def uploadRoute(implicit mat: ActorMaterializer): Route =
    pathPrefix("upload") {
      pathEndOrSingleSlash {
        getFromResource("web/index.html")
      } ~
        path("image") {
          uploadImage
        }
    }

  private def uploadImage(implicit materializer: ActorMaterializer): Route =
    fileUpload("file") {
      case (metadata, byteSource) =>
        if (metadata.fileName.toLowerCase.endsWith(".jpg") || metadata.fileName.toLowerCase
              .endsWith(".jpeg")) {

          val fUploaded = uploadToS3(byteSource, "pixels-demo", s"images/${metadata.fileName}")

          onComplete(fUploaded) {
            case Success(_) =>
              complete(StatusCodes.Accepted)
            case Failure(e) =>
              e.printStackTrace()
              complete(ToResponseMarshallable(StatusCodes.BadRequest))
          }
        } else {
          complete(StatusCodes.BadRequest)
        }
    }

  private def uploadToS3(
      source: Source[ByteString, _],
      bucket: String,
      bucketKey: String
  )(implicit mat: ActorMaterializer): Future[MultipartUploadResult] = {

    val s3Sink: Sink[ByteString, Future[MultipartUploadResult]] =
      S3.multipartUpload(bucket, bucketKey)
    source.runWith(s3Sink)
  }
}
