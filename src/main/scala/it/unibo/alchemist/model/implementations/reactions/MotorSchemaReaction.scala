package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.implementations.actions.{Actuator, MotorSchema}
import it.unibo.alchemist.model.implementations.nodes.MobileNode
import it.unibo.alchemist.model.interfaces._

import scala.collection.JavaConverters._
import it.unibo.alchemist.{Combiner, _}
class MotorSchemaReaction[T, P <: Position[P]](env : Environment[T, P], mobileNode : MobileNode[T, P], distribution: TimeDistribution[T], combiner: Combiner[P])
  extends AbstractReaction[T](mobileNode, distribution) {
  var lastTime : Double = 0.0
  lazy val actuator = new Actuator[T, P](env, mobileNode, env.makePosition(0, 0))
  private def motorSchemas : Seq[MotorSchema[T, P]] = getActions.asScala.collect { case node : MotorSchema[T, P] => node }
  override def updateInternalStatus(curTime: Time, executed: Boolean, env: Environment[T, _]): Unit = {}
  override def cloneOnNewNode(n: Node[T], currentTime: Time): Reaction[T] = new MotorSchemaReaction(env, mobileNode, distribution.clone(currentTime), combiner)
  override def getRate: Double = distribution.getRate
  override def execute(): Unit = {
    val combinedVelocity = combiner.combine(mobileNode, motorSchemas)
    val normalized : P = if(combinedVelocity.module > mobileNode.maximumSpeed) {
      MotorSchemaReaction.normalizedWithSpeed(combinedVelocity, mobileNode, env)
    } else {
      combinedVelocity
    }
    val currentTime = env.getSimulation.getTime.toDouble
    val resultingVelocity = env.makePosition(normalized.getCartesianCoordinates
      .map(x => (x * (currentTime - lastTime)) : Number)
      .toSeq :_*)
    lastTime = currentTime
    actuator.velocity = resultingVelocity
    mobileNode.setVector(resultingVelocity)
    actuator.execute()
  }
}

object MotorSchemaReaction {
  def normalizedWithSpeed[T, P <: Position[P]](velocity : P, mobileNode: MobileNode[T, P], env : Environment[T, P]) : P = {
    val module = velocity.module
    val coordinates = velocity.getCartesianCoordinates.map(cord => (cord / module) * mobileNode.maximumSpeed)
    if(coordinates.exists(value => value.isNaN || value.isInfinite)) {
      env.origin
    } else {
      env.makePosition(coordinates.map(_.asInstanceOf[Number]).toSeq:_*)
    }
  }
}