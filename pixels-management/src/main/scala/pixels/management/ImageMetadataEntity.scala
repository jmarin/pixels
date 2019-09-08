package pixels.management

import akka.Done
import akka.actor.typed.ActorRef
import pixels.metadata.PixelsMetadata
import akka.actor.typed.TypedActorContext
import akka.persistence.typed.scaladsl.EventSourcedBehavior.CommandHandler
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.Effect
import com.typesafe.config.ConfigFactory
import akka.cluster.sharding.typed.scaladsl.Entity
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.actor.typed.SupervisorStrategy
import scala.concurrent.duration._

object ImageMetadataEntity {
  //Command
  sealed trait ImageMetadataCommand
  final case class AddImageMetadata(metadata: PixelsMetadata, replyTo: ActorRef[Done])
      extends ImageMetadataCommand
  final case class RateImage(rating: Int, replyTo: ActorRef[Done]) extends ImageMetadataCommand
  final case class GetImageMetadata(replyTo: ActorRef[Option[PixelsMetadata]])
      extends ImageMetadataCommand
  final case class RemoveImageMetadata(replyTo: ActorRef[Done]) extends ImageMetadataCommand

  //Event
  sealed trait ImageMetadataEvent
  final case class ImageMetadataAdded(metadata: PixelsMetadata) extends ImageMetadataEvent
  final case class ImageMetadataRated(rating: Int) extends ImageMetadataEvent
  case object ImageMetadataRemoved extends ImageMetadataEvent

  //Reply
  case class ImageMetadata(metadata: PixelsMetadata)

  //State
  final case class ImageMetadataState(metadata: Option[PixelsMetadata])

  //Command Handler
  def commandHandler(
      ctx: TypedActorContext[ImageMetadataCommand]
  ): CommandHandler[ImageMetadataCommand, ImageMetadataEvent, ImageMetadataState] = {
    val log = ctx.asScala.log
    (state, cmd) =>
      cmd match {
        case AddImageMetadata(metadata, replyTo) =>
          if (!state.metadata.isDefined) {
            Effect
              .persist(ImageMetadataAdded(metadata))
              .thenRun { _ =>
                log.debug(s"ImageMetadata created: ${metadata.toString}")
                replyTo ! Done
              }
          } else {
            Effect.none
          }

        case GetImageMetadata(replyTo) =>
          replyTo ! state.metadata
          Effect.none

        case RateImage(rating, replyTo) =>
          if (state.metadata.isDefined) {
            Effect
              .persist(ImageMetadataRated(rating))
              .thenRun { _ =>
                log.debug(s"Image rated with rating: $rating")
                replyTo ! Done
              }
          } else {
            Effect.none
          }

        case RemoveImageMetadata(replyTo) =>
          if (state.metadata.isDefined) {
            Effect
              .persist(ImageMetadataRemoved)
              .thenRun { _ =>
                log.debug(s"Image removed: ${state.metadata.getOrElse(PixelsMetadata).toString}")
                replyTo ! Done
              }
          } else {
            Effect.none
          }
      }
  }

  //Event Handler
  def eventHandler: (ImageMetadataState, ImageMetadataEvent) => ImageMetadataState = {
    case (state, ImageMetadataAdded(m)) => state.copy(Some(m))
    case (state, ImageMetadataRated(rating)) =>
      val newMetadata = state.metadata.map(_.copy(rating = rating))
      state.copy(metadata = newMetadata)
    case (state, ImageMetadataRemoved) => state.copy(None)
  }

  def behavior(entityId: String): Behavior[ImageMetadataCommand] =
    Behaviors.setup { ctx =>
      ctx.log.info(s"Started ImageMetadata: $entityId")
      EventSourcedBehavior[ImageMetadataCommand, ImageMetadataEvent, ImageMetadataState](
        persistenceId = PersistenceId(entityId),
        emptyState = ImageMetadataState(None),
        commandHandler = commandHandler(ctx),
        eventHandler = eventHandler
      )
    }

  //TODO: refactor into common code

  val name: String = "ImageMetadata"

  val config = ConfigFactory.load()
  val minBackOff = config.getInt("supervisor.minBackOff")
  val maxBackOff = config.getInt("supervisor.maxBackOff")
  val rFactor = config.getDouble("supervisor.randomFactor")

  val supervisorStrategy = SupervisorStrategy.restartWithBackoff(
    minBackoff = minBackOff.seconds,
    maxBackoff = maxBackOff.seconds,
    randomFactor = rFactor
  )

  def supervisedBehavior(entityId: String): Behavior[ImageMetadataCommand] =
    Behaviors
      .supervise(behavior(entityId))
      .onFailure(supervisorStrategy)

  val TypeKey = EntityTypeKey[ImageMetadataCommand](name)

  def startShardRegion(
      sharding: ClusterSharding
  ): ActorRef[ShardingEnvelope[ImageMetadataCommand]] =
    sharding.init(
      Entity(
        typeKey = TypeKey,
        createBehavior = ctx => supervisedBehavior(ctx.entityId)
      )
    )
}
