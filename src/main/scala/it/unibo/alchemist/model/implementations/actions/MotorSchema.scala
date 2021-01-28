package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist._
import it.unibo.alchemist.model.implementations.nodes.MobileNode
import it.unibo.alchemist.model.interfaces.{Environment, Position}
abstract class MotorSchema[T, P <: Position[P]](env : Environment[T, P], mobileNode: MobileNode[T, P], weight : Double) extends AbstractMoveNode[T, P](env, mobileNode) {
  //TEMPLATE METHOD
  final def velocityVector : P = env.makePosition(unweightedVector.getCartesianCoordinates
    .map(_ * weight)
    .map(_ * mobileNode.maximumSpeed)
    .map(x => x : Number)
    .toSeq :_*)
  override def getNextPosition: P = {
    val velocity = velocityVector
    val droneVelocity = if(velocity.module > mobileNode.maximumSpeed) {
      normalizedWithSpeed(velocity)
    } else {
      velocity
    }
    env.makePosition(droneVelocity.getCartesianCoordinates
      .map(x => x : Number)
      .toSeq :_*)
  }
  override def execute(): Unit = {
    super.execute()
    mobileNode.setVector(getNextPosition)
  }
  protected def unweightedVector : P //abstract method
  protected def getNeighbourOf(drone : MobileNode[T, P]) : Seq[MobileNode[T, P]] = env.getNeighborhood(drone)
    .getNeighbors
    .collect { case node : MobileNode[T, P] => node }
    .filter { case (node) => node.group == mobileNode.group}
  protected def normalizedWithSpeed(p : P) : P = {
    val module = p.module
    val coordinates = p.getCartesianCoordinates.map(cord => (cord / module) * mobileNode.maximumSpeed)
    if(coordinates.exists(value => value.isNaN || value.isInfinite)) {
      env.origin
    } else {
      env.makePosition(coordinates.map(_.asInstanceOf[Number]).toSeq:_*)
    }
  }
}
