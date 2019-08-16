package pixels.api
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

trait PixelsRoutes {
  val defaultRoute: Route =
    path("pixels") {
      get {
        complete("PIXELS")
      }
    }

  // val deletePictureRoute: Route = ???

  // val filterPicturesRoute: Route = ???

  // val pictureInfoRoute: Route = ???

  // val ratePictureRoute: Route = ???

}
