package pixels.management

import akka.actor.typed.ActorRef
import pixels.metadata.PixelsMetadata
import akka.actor.typed.Behavior
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.ExpectingReply
import akka.persistence.typed.scaladsl.Effect

object ImageInfoEntity {

  //Command
  sealed trait ImageInfoCommand[A] extends ExpectingReply[A]
  final case class AddImageInfo(
      s3Url: String,
      width: Int,
      height: Int
  )(override val replyTo: ActorRef[ImageInfoOperationResult])
      extends ImageInfoCommand[ImageInfoOperationResult]

  final case class RateImage(rating: Int)(override val replyTo: ActorRef[ImageInfoOperationResult])
      extends ImageInfoCommand[ImageInfoOperationResult]
  final case class GetImageInfo()(override val replyTo: ActorRef[ImageMetaData])(s3Url: String)
      extends ImageInfoCommand[ImageMetaData]
  final case class RemoveImageInfo()(override val replyTo: ActorRef[ImageInfoOperationResult])
      extends ImageInfoCommand[ImageInfoOperationResult]

  //Reply
  sealed trait ImageInfoReply
  sealed trait ImageInfoOperationResult extends ImageInfoReply
  case object ImageInfoAccepted extends ImageInfoOperationResult
  case class ImageInfoRejected(reason: String) extends ImageInfoOperationResult
  case class ImageMetaData(metadata: PixelsMetadata) extends ImageInfoReply

  //Event
  sealed trait ImageInfoEvent
  final case class ImageInfoAdded(metadata: PixelsMetadata) extends ImageInfoEvent
  final case class ImageRated(rating: Int) extends ImageInfoEvent
  final case class ImageInfoRemoved(s3URL: String) extends ImageInfoEvent

  // Type alias to reduce boilerplate
  type ReplyEffect = akka.persistence.typed.scaladsl.ReplyEffect[ImageInfoEvent, ImageInfo]

  //State
  sealed trait ImageInfo {
    def applyCommand(cmd: ImageInfoCommand[_]): ReplyEffect
    def applyEvent(event: ImageInfoEvent): ImageInfo
  }

  case object EmptyImageInfo extends ImageInfo {
    override def applyCommand(cmd: ImageInfoCommand[_]): ReplyEffect =
      cmd match {
        case c @ AddImageInfo(s3Url, width, height) =>
          val metadata = PixelsMetadata(s3Url, width, height)
          Effect.persist(ImageInfoAdded(metadata)).thenReply(c)(_ => ImageInfoAccepted)

        case _ =>
          Effect.unhandled.thenNoReply()
      }

    override def applyEvent(event: ImageInfoEvent): ImageInfo = event match {
      case ImageInfoAdded(metadata) => ImageInfoData(metadata)
      case _ =>
        throw new IllegalStateException(s"unexpected event [$event] in state [EmptyImageInfo]")

    }

  }

  case class ImageInfoData(metadata: PixelsMetadata) extends ImageInfo {
    override def applyCommand(cmd: ImageInfoCommand[_]): ReplyEffect =
      cmd match {
        case c @ AddImageInfo(s3Url, width, height) =>
          Effect.unhandled.thenNoReply()
        case c @ GetImageInfo() =>
          Effect.unhandled.thenReply(c)(_ => ImageMetaData(metadata))
        case c @ RateImage(rating) =>
          Effect
            .persist(ImageRated(rating))
            .thenReply(c)(_ => ImageInfoAccepted)
        case c @ RemoveImageInfo() =>
          Effect.persist(ImageInfoRemoved(metadata.s3Url)).thenReply(c)(_ => ImageInfoAccepted)
      }

    override def applyEvent(event: ImageInfoEvent): ImageInfo = event match {
      case ImageInfoAdded(metadata) =>
        throw new IllegalStateException(s"unexpected event [$event] in state [ImageInfoData]")
      case c @ ImageRated(rating) =>
        val ratedMetadata = metadata.copy(rating = rating)
        copy(metadata = ratedMetadata)
      case ImageInfoRemoved(s3URL) => ImageInfoRemoved
    }

  }

  case object ImageInfoRemoved extends ImageInfo {
    override def applyCommand(cmd: ImageInfoCommand[_]): ReplyEffect =
      cmd match {
        case c: AddImageInfo =>
          Effect.reply(c)(ImageInfoRejected("Image info has been removed"))
        case c: GetImageInfo =>
          Effect.reply(c)(ImageMetaData(PixelsMetadata()))
        case c: RateImage =>
          Effect.reply(c)(ImageInfoRejected("Image info has been removed"))
        case c: RemoveImageInfo =>
          Effect.reply(c)(ImageInfoRejected("Image info has been removed"))
      }

    override def applyEvent(event: ImageInfoEvent): ImageInfo =
      throw new IllegalStateException(s"unexpected event [$event] in state [ImageInfoRemoved]")

  }

  def behavior(entityId: String): Behavior[ImageInfoCommand[ImageInfoReply]] = {
    EventSourcedBehavior
      .withEnforcedReplies[ImageInfoCommand[ImageInfoReply], ImageInfoEvent, ImageInfo](
        PersistenceId(s"$entityId"),
        EmptyImageInfo,
        (state, cmd) => state.applyCommand(cmd),
        (state, event) => state.applyEvent(event)
      )
  }

}
