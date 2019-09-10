package pixels.api

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import scala.util.{Success, Failure}
import akka.http.scaladsl.model.MediaTypes
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.stream.scaladsl.Sink
import pixels.persistence.ImageEntity
import pixels.persistence.ImageEntity.{AddImage, GetImage}
import scala.concurrent.Future
import akka.Done
import pixels.persistence.ImageEntity._
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import akka.actor.typed.DispatcherSelector
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentType

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

      val imageEntity = sharding.entityRefFor(ImageEntity.TypeKey, s"${ImageEntity.name}-$id")

      get {
        val fBytes: Future[Array[Byte]] = for {
          image <- (imageEntity ? (ref => GetImage(ref)))
          bytes = image.map(_.bytes).getOrElse(Array.empty[Byte])
        } yield bytes

        onComplete(fBytes) {
          case Success(bytes) =>
            if (bytes.size > 0)
              complete(HttpEntity(ContentType.Binary(MediaTypes.`image/jpeg`), bytes))
            else
              complete(StatusCodes.NotFound)

          case Failure(e) =>
            complete(StatusCodes.InternalServerError)
        }
      } ~
        delete {
          val fRemoved = imageEntity ? (ref => RemoveImage(ref))

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

          val imageEntity =
            sharding.entityRefFor(ImageEntity.TypeKey, s"${ImageEntity.name}-$id")

          val fDone: Future[Done] = for {
            byteString <- byteSource.runWith(Sink.seq)
            bytes = byteString.flatMap(_.toList).toArray
            done <- imageEntity ? (ref => AddImage(id, bytes, ref))
          } yield done

          onComplete(fDone) {
            case Success(d) =>
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
