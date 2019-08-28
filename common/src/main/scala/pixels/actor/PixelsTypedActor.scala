package pixels.actor

import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.ConfigFactory
import akka.actor.typed.Behavior
import akka.actor.typed.SupervisorStrategy
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import scala.reflect.ClassTag
import akka.actor.typed.ActorRef
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.Entity
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey

trait PixelsTypedActor[A] {

  val name: String

  val config = ConfigFactory.load()
  val minBackOff = config.getInt("supervisor.minBackOff")
  val maxBackOff = config.getInt("supervisor.maxBackOff")
  val rFactor = config.getDouble("supervisor.randomFactor")

  def typeKey(implicit tag: ClassTag[A]): EntityTypeKey[A] =
    EntityTypeKey[A](name)

  def behavior(entityId: String): Behavior[A]

  val supervisorStrategy: SupervisorStrategy

  protected def supervisedBehavior(entityId: String): Behavior[A] = {
    Behaviors
      .supervise(behavior(entityId))
      .onFailure(supervisorStrategy)
  }

  def startShardRegion(
      sharding: ClusterSharding
  )(implicit tag: ClassTag[A]): ActorRef[ShardingEnvelope[A]] =
    sharding.init(
      Entity(
        typeKey = typeKey,
        createBehavior = ctx => supervisedBehavior(ctx.entityId)
      )
    )

}
