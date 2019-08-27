package pixels.management

import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.actor.typed.Behavior
import akka.cluster.sharding.typed.scaladsl.EventSourcedEntity
import pixels.metadata.PixelsMetadata

object PixelsManagement {

  // Commands

  sealed trait PixelsManagementCommand

  final case class AddImageInfo(
      s3Url: String,
      width: Int,
      height: Int,
      replyTo: ActorRef[PixelsMetadata]
  ) extends PixelsManagementCommand

  final case class RateImage(rating: Int, replyTo: ActorRef[Rating]) extends PixelsManagementCommand

  final case class GetImage(s3Url: String, replyTo: ActorRef[PixelsMetadata])
      extends PixelsManagementCommand

  final case class RemoveImage(replyTo: ActorRef[PixelsRemoved]) extends PixelsManagementCommand

  // Responses

  sealed trait PixelsManagementResponse

  final case class PixelsManagementSuccess() extends PixelsManagementResponse

  final case class Rating(s3Url: String, rating: Int) extends PixelsManagementResponse

  // Events
  sealed trait PixelsManagementEvent

  final case class PixelsInfoAdded(metadata: PixelsMetadata) extends PixelsManagementEvent

  final case class PixelsRated(rating: Int) extends PixelsManagementEvent

  final case class PixelsRemoved(s3Url: String) extends PixelsManagementEvent

  // State

  case class PixelState(metadata: PixelsMetadata) {
    def add(evt: PixelsManagementEvent): PixelState = evt match {
      case PixelsInfoAdded(metadata) => ???
      case PixelsRated(rating)       => ???
      case PixelsRemoved(s3Url)      => ???
    }
  }

  val entityTypeKey: EntityTypeKey[PixelsManagementCommand] =
    EntityTypeKey[PixelsManagementCommand]("pixels-management")

  def persistentEntity(entityId: String): Behavior[PixelsManagementCommand] =
    EventSourcedEntity(
      entityTypeKey = entityTypeKey,
      entityId = entityId,
      emptyState = PixelState(PixelsMetadata()),
      commandHandler,
      eventHandler
    )

  private val commandHandler
      : (PixelState, PixelsManagementCommand) => Effect[PixelsManagementEvent, PixelState] = {
    (prevState, cmd) =>
      cmd match {
        case AddImageInfo(s3Url, width, height, replyTo) =>
          Effect
            .persist({ PixelsInfoAdded(PixelsMetadata(s3Url, width, height)) })
            .thenRun(state => replyTo ! state.metadata)

        case GetImage(s3Url, replyTo) =>
          Effect.none
            .thenRun(state => replyTo ! state.metadata)

        case RateImage(rating, replyTo) =>
          Effect
            .persist(PixelsRated(rating))
            .thenRun(state => Rating(state.metadata.s3Url, rating))

        case RemoveImage(replyTo) =>
          Effect
            .persist(PixelsRemoved(prevState.metadata.s3Url))
            .thenRun(state => replyTo ! PixelsRemoved(state.metadata.s3Url))

      }
  }

  private val eventHandler: (PixelState, PixelsManagementEvent) => PixelState = ???

}
