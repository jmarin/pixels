package pixels.actor
import akka.actor.typed.TypedActorContext
import akka.persistence.typed.scaladsl.EventSourcedBehavior.CommandHandler

trait PixelsTypedPersistentActor[C, E, S] extends PixelsTypedActor[C] {
  def commandHandler(ctx: TypedActorContext[C]): CommandHandler[C, E, S]
  def eventHandler: (S, E) => S
}
