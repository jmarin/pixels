package pixels.api

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import scala.util.{Success, Failure}
import akka.http.scaladsl.model.MediaTypes
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.stream.scaladsl.Sink
import scala.concurrent.Future
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import akka.actor.typed.DispatcherSelector
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentType
import pixels.repository.ImageRepositoryImpl
import pixels.service.ImageServiceImpl
import pixels.model.Image

trait ImageRoute {

  val sharding: ClusterSharding

  implicit val timeout: Timeout

  def uploadRoute(implicit system: ActorSystem[_], mat: ActorMaterializer): Route =
    path("images") {
      uploadImage
    }

  def imageRoute(implicit system: ActorSystem[_]): Route =
    path("images" / Segment) { id =>
      implicit val ec = system.dispatchers.lookup(DispatcherSelector.default())
      val imageService = ImageServiceImpl(id, ImageRepositoryImpl(id, sharding))
      get {
        parameters('scale.as[Double] ? 1.0, 'monochrome.as[Boolean] ? false) { (s, m) =>
          val fImage: Future[Image] = imageService.get
            .map(i => if (m) imageService.grayscale(i) else i)
            .map(
              i =>
                if (s < 1)
                  imageService
                    .resize(i, (i.metadata.width * s).toInt, (i.metadata.height * s).toInt)
                else
                  i
            )

          val fBytes = fImage.map(_.data.bytes)

          onComplete(fBytes) {
            case Success(bytes) =>
              if (bytes.size > 0)
                complete(HttpEntity(ContentType.Binary(MediaTypes.`image/jpeg`), bytes))
              else
                complete(StatusCodes.NotFound)

            case Failure(e) =>
              complete(StatusCodes.InternalServerError)
          }
        }
      } ~
        delete {
          val fRemoved = imageService.remove(id)

          onComplete(fRemoved) {
            case Success(done) =>
              complete(StatusCodes.NoContent)
            case Failure(e) =>
              complete(StatusCodes.BadRequest)
          }
        }
    }

  private def uploadImage(
      implicit system: ActorSystem[_],
      materializer: ActorMaterializer
  ): Route = {
    fileUpload("file") {
      case (metadata, byteSource) =>
        if (metadata.fileName.toLowerCase.endsWith(".jpg") || metadata.fileName.toLowerCase
              .endsWith(".jpeg")) {

          implicit val ec = system.dispatchers.lookup(DispatcherSelector.default())

          val id = metadata.fileName

          val imageService = ImageServiceImpl(id, ImageRepositoryImpl(id, sharding))

          val fAdded: Future[String] = for {
            byteString <- byteSource.runWith(Sink.seq)
            bytes = byteString.flatMap(_.toList).toArray
            i <- imageService.addImage(bytes)
          } yield i

          onComplete(fAdded) {
            case Success(i) =>
              complete(StatusCodes.Created)
            case Failure(e) =>
              println(e)
              complete(StatusCodes.InternalServerError)
          }
        } else {
          complete(StatusCodes.BadRequest)
        }

    }
  }
}
