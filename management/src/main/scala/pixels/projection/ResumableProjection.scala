package pixels.projection

import akka.actor.typed.ActorRef
import akka.persistence.query.Offset
import akka.persistence.query.NoOffset
import akka.persistence.query.EventEnvelope
import akka.Done
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.scaladsl.EventSourcedBehavior.CommandHandler
import akka.persistence.typed.PersistenceId
import akka.actor.typed.TypedActorContext
import akka.persistence.typed.scaladsl.Effect
import akka.actor.TypedActor.PostRestart
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.stream.scaladsl.RestartSource
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Sink
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import akka.actor.typed.DispatcherSelector
import akka.stream.ActorMaterializer
import akka.actor.typed.scaladsl.adapter._
import scala.concurrent.ExecutionContext

object ResumableProjection extends PersistenceQuery {

  val name = "PixelsProjection"

  implicit val timeout: Timeout = Timeout(5.seconds)

  //Commands
  sealed trait ProjectionCommand
  case class SaveOffset(offset: Offset, replyTo: ActorRef[Done]) extends ProjectionCommand
  case class GetOffset(replyTo: ActorRef[Offset]) extends ProjectionCommand
  case object StartStreaming extends ProjectionCommand

  //Events
  sealed trait ProjectionEvent
  case class OffsetSaved(offset: Offset) extends ProjectionEvent

  //State
  case class ProjectionState(offset: Offset = NoOffset)

  def behavior(tag: String): Behavior[ProjectionCommand] = Behaviors.setup { ctx =>
    EventSourcedBehavior[ProjectionCommand, ProjectionEvent, ProjectionState](
      persistenceId = PersistenceId(name),
      emptyState = ProjectionState(),
      commandHandler = commandHandler(ctx, tag),
      eventHandler = eventHandler
    ).receiveSignal {
      case (_, _: PostRestart) => ctx.self ! StartStreaming
    }
  }

  def commandHandler(
      ctx: TypedActorContext[ProjectionCommand],
      tag: String
  ): CommandHandler[ProjectionCommand, ProjectionEvent, ProjectionState] = { (state, cmd) =>
    val log = ctx.asScala.log
    cmd match {
      case StartStreaming =>
        log.info("Start Streaming for events tagged with {}", tag)
        runQueryStream(ctx, tag)

        Effect.none
      case SaveOffset(offset, replyTo) =>
        Effect.persist(OffsetSaved(offset)).thenRun { _ =>
          log.info("Offset saved: {}", offset)
          replyTo ! Done
        }

      case GetOffset(replyTo) =>
        replyTo ! state.offset
        Effect.none

    }
  }

  def eventHandler: (ProjectionState, ProjectionEvent) => ProjectionState = {
    case (state, OffsetSaved(offset)) => state.copy(offset = offset)
  }

  private def runQueryStream(ctx: TypedActorContext[ProjectionCommand], tag: String): Unit = {
    val log = ctx.asScala.log
    val self = ctx.asScala.self
    implicit val system = ctx.asScala.system
    implicit val untypedSystem = system.toUntyped
    implicit val materializer = ActorMaterializer()(untypedSystem)
    implicit val scheduler = ctx.asScala.system.scheduler
    implicit val ec = ctx.asScala.system.dispatchers.lookup((DispatcherSelector.default()))
    RestartSource
      .withBackoff(
        minBackoff = 500.millis,
        maxBackoff = 20.seconds,
        randomFactor = 0.1,
        maxRestarts = 10
      ) { () =>
        Source.fromFutureSource {
          (self ? (ref => GetOffset(ref))).map { offset =>
            log.info("Streaming events for tag [{}] from offset [{}]", tag, offset)
            readJournal(ctx.asScala.system)
              .eventsByTag(tag, offset)
              .mapAsync(1)(env => projectEvent(ctx, env))
          }
        }
      }
      .runWith(Sink.ignore)
  }

  def projectEvent(
      ctx: TypedActorContext[ProjectionCommand],
      envelope: EventEnvelope
  )(implicit ec: ExecutionContext): Future[EventEnvelope] = {
    val log = ctx.asScala.log
    log.info("Projecting event envelope {}", envelope.toString())
    Future(envelope)
  }
}
