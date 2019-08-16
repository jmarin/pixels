package pixels.api

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import scala.concurrent.ExecutionContext
import akka.stream.ActorMaterializer
import akka.Done
import akka.actor.typed.scaladsl.adapter._
import scala.concurrent.Future
import akka.http.scaladsl.Http
import scala.util.{Success, Failure}
import akka.http.scaladsl.Http.ServerBinding

object PixelsApi extends App with PixelsRoutes {
  val name = "pixels-api"
  val host: String = "localhost"
  val port: Int = 8080

  val system = ActorSystem[Done](
    Behaviors.setup { ctx =>
      implicit val untypedSystem = ctx.system.toUntyped
      implicit val materializer: ActorMaterializer = ActorMaterializer()(untypedSystem)
      implicit val ec: ExecutionContext = ctx.system.executionContext

      val routes = defaultRoute

      val serverBinding: Future[ServerBinding] =
        Http()(untypedSystem).bindAndHandle(routes, host, port)

      serverBinding.onComplete {
        case Success(bound) =>
          println(s"Server running at $host:$port")
        case Failure(e) =>
          e.printStackTrace()
          ctx.self ! Done
      }

      Behaviors.receiveMessage {
        case Done => Behaviors.stopped
      }

    },
    name
  )
}
