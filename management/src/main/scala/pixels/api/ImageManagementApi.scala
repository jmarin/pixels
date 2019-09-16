package pixels.api

import akka.actor.typed.{ActorSystem, ActorRef}
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
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import scala.concurrent.duration._
import pixels.persistence.ImageEntity
import pixels.persistence.MetadataEntity
import pixels.projection.ResumableProjection
import akka.cluster.typed.ClusterSingleton
import akka.cluster.typed.SingletonActor
import akka.stream.ActorAttributes.SupervisionStrategy
import akka.actor.typed.SupervisorStrategy
import pixels.query.MetadataComponent
import pixels.common.db.DbConfiguration._

object ImageManagementApi extends App with ImageRoute with MetadataComponent {
  val config = ConfigFactory.load()

  val name = config.getString("pixels.management.api.http.name")
  val host: String = config.getString("pixels.management.api.http.host")
  val port: Int = config.getInt("pixels.management.api.http.port")

  implicit val timeout = config.getInt("pixels.management.api.http.timeout").seconds

  val system = ActorSystem[Done](
    Behaviors.setup { ctx =>
      implicit val system = ctx.system
      implicit val untypedSystem = ctx.system.toUntyped
      implicit val materializer: ActorMaterializer = ActorMaterializer()(untypedSystem)
      implicit val ec: ExecutionContext = ctx.system.executionContext

      val routes = uploadRoute ~ imageRoute

      val serverBinding: Future[ServerBinding] =
        Http()(untypedSystem).bindAndHandle(routes, host, port)

      val singletonManager = ClusterSingleton(system)

      val metadataDbRepository = new MetadataDbRepository(dbConfig)

      metadataDbRepository.createSchema().map(_ => println("Database schema created"))

      val projection: ActorRef[ResumableProjection.ProjectionCommand] = singletonManager.init(
        SingletonActor(
          Behaviors
            .supervise(ResumableProjection.behavior("metadata"))
            .onFailure[Exception](SupervisorStrategy.restartWithBackoff(1.second, 10.seconds, 0.2)),
          "MetadataProjection"
        )
      )

      projection ! ResumableProjection.StartStreaming

      serverBinding.onComplete {
        case Success(bound) =>
          println(s"Service $name running at $host:$port")
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

  override val sharding: ClusterSharding = ClusterSharding(system)

  ImageEntity.startShardRegion(sharding)
  MetadataEntity.startShardRegion(sharding)

}
