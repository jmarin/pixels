package pixels.search

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

trait SearchRoute {
  def searchRoute: Route =
    path("search") {
      complete("OK")
    }
}
