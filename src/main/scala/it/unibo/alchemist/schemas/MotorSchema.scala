package it.unibo.alchemist.schemas

import it.unibo.alchemist.model.implementations.actions.AbstractMoveNode
import it.unibo.alchemist.model.interfaces.{Action, Environment, Position}
import it.unibo.alchemist.node.DroneNode
import it.unibo.alchemist._

abstract class MotorSchema[T, P <: Position[P]](env : Environment[T, P], droneNode: DroneNode[T, P], weight : Double) extends AbstractMoveNode[T, P](env, droneNode) {
  final def velocityVector : P = env.makePosition(unweightedVector.getCartesianCoordinates
    .map(_ * weight)
    .map(x => (x : Number))
    .toSeq :_*)

  protected def unweightedVector : P

  override def getNextPosition: P = {
    val velocity = velocityVector
    val droneVelocity = if(velocity.module > droneNode.speed) {
      versor(velocity)
    } else {
      velocity
    }
    env.makePosition(droneVelocity.getCartesianCoordinates
      .map(x => (x : Number))
      .toSeq :_*)
  }

  override def execute(): Unit = {
    super.execute()
    droneNode.setVector(getNextPosition)
  }

  protected def getNeighbourOf(drone : DroneNode[T, P]) : Seq[DroneNode[T, P]] = env.getNeighborhood(drone)
    .getNeighbors
    .collect { case node : DroneNode[T, P] => node }

  def versor(p : P) : P = {
    val module = p.module
    val coordinates = p.getCartesianCoordinates.map(_ / module)
    if(coordinates.exists(value => value.isNaN || value.isInfinite)) {
      env.origin
    } else {
      env.makePosition(coordinates.map((_.asInstanceOf[Number])).toSeq:_*)
    }
  }
}
