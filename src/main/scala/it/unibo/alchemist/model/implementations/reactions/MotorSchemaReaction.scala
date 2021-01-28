package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.interfaces._

import scala.collection.JavaConverters._
import it.unibo.alchemist.{Combiner, _}
import it.unibo.alchemist.model.implementations.actions.{Actuator, MotorSchema}
import it.unibo.alchemist.model.implementations.nodes.MobileNode
class MotorSchemaReaction[T, P <: Position[P]](env : Environment[T, P], drone : MobileNode[T, P], distribution: TimeDistribution[T], combiner: Combiner[P])
  extends AbstractReaction[T](drone, distribution) {
  val rate = distribution.getRate
  private def motorSchemas : Seq[MotorSchema[T, P]] = getActions.asScala.collect { case node : MotorSchema[T, P] => node }
  override def updateInternalStatus(curTime: Time, executed: Boolean, env: Environment[T, _]): Unit = {}
  override def cloneOnNewNode(n: Node[T], currentTime: Time): Reaction[T] = new MotorSchemaReaction(env, drone, distribution.clone(currentTime), combiner)
  override def getRate: Double = distribution.getRate
  override def execute(): Unit = {
    val movement = combiner.combine(drone, motorSchemas)
    val normalized = env.makePosition(movement.getCartesianCoordinates.map(_ * 1 / rate).map(_.asInstanceOf[Number]):_*)
    new Actuator[T,P](env, drone, normalized).execute()
  }

}
