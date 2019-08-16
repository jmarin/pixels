package pixels.api

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.stream.scaladsl.Sink
import akka.stream.ActorMaterializer
import scala.util.{Success, Failure}

trait UploadRoute {

  def uploadImageRoute(implicit mat: ActorMaterializer): Route =
    path("upload") {
      uploadImage
    }

  private def uploadImage(implicit materializer: ActorMaterializer): Route = {
    fileUpload("picture") {
      case (metadata, byteSource)
          if metadata.fileName.toLowerCase.contains(".jpg") || metadata.fileName.toLowerCase
            .contains(".jpeg") =>
        val fUploaded = byteSource
          .runWith(Sink.ignore)

        onComplete(fUploaded) {
          case Success(_) =>
            complete(StatusCodes.Accepted)
          case Failure(e) =>
            complete(ToResponseMarshallable(StatusCodes.BadRequest))
        }
      case _ =>
        complete(ToResponseMarshallable(StatusCodes.BadRequest))
    }
  }
}
