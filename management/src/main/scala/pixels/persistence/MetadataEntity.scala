package pixels.persistence

import akka.Done
import akka.actor.typed.ActorRef
import akka.actor.typed.TypedActorContext
import akka.persistence.typed.scaladsl.EventSourcedBehavior.CommandHandler
import akka.actor.typed.Behavior
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.cluster.sharding.typed.scaladsl.Entity
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.ConfigFactory
import akka.actor.typed.SupervisorStrategy
import scala.concurrent.duration._
import pixels.metadata.ImageMetadata
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.PersistenceId

object MetadataEntity {

  //Command
  sealed trait MetadataCommand
  final case class AddMetadata(metadata: ImageMetadata, replyTo: ActorRef[Done])
      extends MetadataCommand
  final case class GetMetadata(replyTo: ActorRef[Option[ImageMetadata]]) extends MetadataCommand
  final case class RemoveMetadata(replyTo: ActorRef[Done]) extends MetadataCommand

  //Event
  sealed trait MetadataEvent
  final case class MetadataAdded(metadata: ImageMetadata) extends MetadataEvent
  final case object MetadataRemoved extends MetadataEvent

  //State
  case class MetadataState(metadata: Option[ImageMetadata])

  //Command Handler
  def commandHandler(
      ctx: TypedActorContext[MetadataCommand],
      entityId: String
  ): CommandHandler[MetadataCommand, MetadataEvent, MetadataState] = {
    val log = ctx.asScala.log
    (state, cmd) =>
      cmd match {
        case AddMetadata(metadata, replyTo) =>
          if (!state.metadata.isDefined) {
            Effect.persist(MetadataAdded(metadata)).thenRun { _ =>
              log.debug(s"Persisted metadata $metadata for image $entityId")
              replyTo ! Done
            }
          } else {
            Effect.none
          }
        case GetMetadata(replyTo) =>
          replyTo ! state.metadata
          Effect.none

        case RemoveMetadata(replyTo) =>
          if (state.metadata.isDefined) {
            Effect.persist(MetadataRemoved).thenRun { _ =>
              log.debug(s"Removed metadata from image $entityId")
              replyTo ! Done
            }
          } else {
            Effect.none
          }
      }
  }

  //Event Handler
  def eventHandler: (MetadataState, MetadataEvent) => MetadataState = {
    case (state, MetadataAdded(m)) => state.copy(Some(m))
    case (state, MetadataRemoved)  => MetadataState(None)
  }

  //TODO: refactor into common code

  val name: String = "MetadataEntity"

  val config = ConfigFactory.load()
  val minBackOff = config.getInt("supervisor.minBackOff")
  val maxBackOff = config.getInt("supervisor.maxBackOff")
  val rFactor = config.getDouble("supervisor.randomFactor")

  val supervisorStrategy = SupervisorStrategy.restartWithBackoff(
    minBackoff = minBackOff.seconds,
    maxBackoff = maxBackOff.seconds,
    randomFactor = rFactor
  )

  def behavior(entityId: String): Behavior[MetadataCommand] =
    Behaviors.setup { ctx =>
      ctx.log.info(s"Started MetadataEntity: $entityId")
      EventSourcedBehavior[MetadataCommand, MetadataEvent, MetadataState](
        persistenceId = PersistenceId(entityId),
        emptyState = MetadataState(None),
        commandHandler = commandHandler(ctx, entityId),
        eventHandler = eventHandler
      ).withTagger(_ => Set("metadata"))
    }

  def supervisedBehavior(entityId: String): Behavior[MetadataCommand] =
    Behaviors
      .supervise(behavior(entityId))
      .onFailure(supervisorStrategy)

  val TypeKey = EntityTypeKey[MetadataCommand](name)

  def startShardRegion(sharding: ClusterSharding): ActorRef[ShardingEnvelope[MetadataCommand]] =
    sharding.init(
      Entity(
        typeKey = TypeKey,
        createBehavior = ctx => supervisedBehavior(ctx.entityId)
      )
    )

}
