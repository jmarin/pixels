package pixels.search

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import scala.concurrent.ExecutionContext
import akka.stream.ActorMaterializer
import akka.Done
import akka.actor.typed.scaladsl.adapter._
import scala.concurrent.Future
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import scala.util.{Success, Failure}
import akka.http.scaladsl.Http.ServerBinding
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

object PixelsSearchMain extends App with SearchRoute {
  val config = ConfigFactory.load()

  val name = config.getString("pixels.search.api.http.name")
  val host: String = config.getString("pixels.search.api.http.host")
  val port: Int = config.getInt("pixels.search.api.http.port")

  implicit val timeout = config.getInt("pixels.search.api.http.timeout").seconds

  // val system = ActorSystem[Done](
  //   Behaviors.setup { ctx =>
  //     implicit val system = ctx.system
  //     implicit val untypedSystem = ctx.system.toUntyped
  //     implicit val materializer: ActorMaterializer = ActorMaterializer()(untypedSystem)
  //     implicit val ec: ExecutionContext = ctx.system.executionContext

  //     val routes = searchRoute

  //     val serverBinding: Future[ServerBinding] =
  //       Http()(untypedSystem).bindAndHandle(routes, host, port)

  //     serverBinding.onComplete {
  //       case Success(bound) =>
  //         println(s"Service $name running at $host:$port")
  //       case Failure(e) =>
  //         e.printStackTrace()
  //         ctx.self ! Done
  //     }

  //     Behaviors.receiveMessage {
  //       case Done => Behaviors.stopped
  //     }
  //   },
  //   name
  // )

}
