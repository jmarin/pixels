package pixels.api

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import scala.util.{Success, Failure}
import pixels.s3.S3Utils
import akka.http.scaladsl.model.MediaTypes
import akka.cluster.sharding.typed.scaladsl.ClusterSharding

trait ImageRoute extends S3Utils {

  val sharding: ClusterSharding

  def uploadRoute(implicit mat: ActorMaterializer): Route =
    path("images") {
      uploadImage
    }

  def deleteRoute: Route =
    path("images" / Segment) { id =>
      complete("deleted")
    }

  private def uploadImage(implicit materializer: ActorMaterializer): Route =
    fileUpload("file") {
      case (metadata, byteSource) =>
        if (metadata.fileName.toLowerCase.endsWith(".jpg") || metadata.fileName.toLowerCase
              .endsWith(".jpeg")) {

          val fUploaded = uploadToS3(
            byteSource,
            "pixels-demo",
            s"images/${metadata.fileName}",
            MediaTypes.`image/jpeg`
          )

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

}
