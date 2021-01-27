package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.interfaces._

import scala.collection.JavaConverters._
import it.unibo.alchemist.{Combiner, _}
import it.unibo.alchemist.model.implementations.actions.{Actuator, MotorSchema}
import it.unibo.alchemist.model.implementations.nodes.DroneNode
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
