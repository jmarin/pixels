package pixels.management

import akka.actor.typed.ActorRef
import akka.actor.typed.TypedActorContext
import akka.actor.typed.Behavior
import akka.persistence.typed.scaladsl.EventSourcedBehavior.CommandHandler
import akka.actor.typed.scaladsl.Behaviors
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.Effect
import com.typesafe.config.ConfigFactory
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.Entity
import akka.actor.typed.SupervisorStrategy
import scala.concurrent.duration._
import akka.Done

object ImageEntity {

  //Command
  sealed trait ImageCommand
  final case class AddImage(id: String, bytes: Array[Byte], replyTo: ActorRef[Done])
      extends ImageCommand
  final case class GetImage(replyTo: ActorRef[Option[Image]]) extends ImageCommand
  final case class RemoveImage(replyTo: ActorRef[Done]) extends ImageCommand

  //Event
  sealed trait ImageEvent
  final case class ImageAdded(id: String, bytes: Array[Byte]) extends ImageEvent
  final case class ImageRemoved(id: String) extends ImageEvent

  //Reply
  case class Image(id: String, bytes: Array[Byte])

  //State
  final case class ImageState(image: Option[Image] = None)

  //Command Handler
  def commandHandler(
      ctx: TypedActorContext[ImageCommand],
      entityId: String
  ): CommandHandler[ImageCommand, ImageEvent, ImageState] = {
    val log = ctx.asScala.log
    (state, cmd) =>
      cmd match {
        case AddImage(id, bytes, replyTo) =>
          if (!state.image.isDefined) {
            Effect.persist(ImageAdded(id, bytes)).thenRun { _ =>
              log.debug(s"Persisted image $id")
              replyTo ! Done
            }
          } else {
            Effect.none
          }

        case GetImage(replyTo) =>
          replyTo ! state.image
          Effect.none
        case RemoveImage(replyTo) =>
          if (state.image.isDefined) {
            Effect.persist(ImageRemoved(entityId)).thenRun { _ =>
              log.debug(s"Removed image $entityId")
              replyTo ! Done
            }
          } else {
            Effect.none
          }

      }
  }

  //Event Handler
  def eventHandler: (ImageState, ImageEvent) => ImageState = {
    case (state, ImageAdded(id, bytes)) => state.copy(Some(Image(id, bytes)))
    case (state, ImageRemoved(id))      => state.copy(None)
  }

  def behavior(entityId: String): Behavior[ImageCommand] =
    Behaviors.setup { ctx =>
      ctx.log.info(s"Started ImageEntity: $entityId")
      EventSourcedBehavior[ImageCommand, ImageEvent, ImageState](
        persistenceId = PersistenceId(entityId),
        emptyState = ImageState(),
        commandHandler = commandHandler(ctx, entityId),
        eventHandler = eventHandler
      )
    }

  //TODO: refactor into common code

  val name: String = "ImageEntity"

  val config = ConfigFactory.load()
  val minBackOff = config.getInt("supervisor.minBackOff")
  val maxBackOff = config.getInt("supervisor.maxBackOff")
  val rFactor = config.getDouble("supervisor.randomFactor")

  val supervisorStrategy = SupervisorStrategy.restartWithBackoff(
    minBackoff = minBackOff.seconds,
    maxBackoff = maxBackOff.seconds,
    randomFactor = rFactor
  )

  def supervisedBehavior(entityId: String): Behavior[ImageCommand] =
    Behaviors
      .supervise(behavior(entityId))
      .onFailure(supervisorStrategy)

  val TypeKey = EntityTypeKey[ImageCommand](name)

  def startShardRegion(sharding: ClusterSharding): ActorRef[ShardingEnvelope[ImageCommand]] =
    sharding.init(
      Entity(
        typeKey = TypeKey,
        createBehavior = ctx => supervisedBehavior(ctx.entityId)
      )
    )

}
