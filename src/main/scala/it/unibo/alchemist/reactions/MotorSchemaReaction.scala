package it.unibo.alchemist.reactions

import it.unibo.alchemist.model.implementations.reactions.AbstractReaction
import it.unibo.alchemist.model.interfaces._
import it.unibo.alchemist.node.DroneNode
import it.unibo.alchemist.schemas.MotorSchema

import scala.collection.JavaConverters._
import it.unibo.alchemist._
import it.unibo.alchemist.actions.{Actuator, Combiner}
class MotorSchemaReaction[T, P <: Position[P]](env : Environment[T, P], drone : DroneNode[T, P], distribution: TimeDistribution[T], combiner: Combiner[P])
  extends AbstractReaction[T](drone, distribution) {
  private def motorSchemas : Seq[MotorSchema[T, P]] = getActions.asScala.collect { case node : MotorSchema[T, P] => node }
  override def updateInternalStatus(curTime: Time, executed: Boolean, env: Environment[T, _]): Unit = {}
  override def cloneOnNewNode(n: Node[T], currentTime: Time): Reaction[T] = new MotorSchemaReaction(env, drone, distribution.clone(currentTime), combiner)
  override def getRate: Double = distribution.getRate
  override def execute(): Unit = {
    val movement = drone.currentVector + combiner.combine(motorSchemas)
    new Actuator[T,P](env, drone, movement).execute()
  }

}
