package it.unibo.alchemist.actions

import it.unibo.alchemist.model.interfaces.{Action, Environment, Node, Position, Reaction}
import it.unibo.alchemist.node.DroneNode
import it.unibo.alchemist.schemas.MotorSchema

class Actuator[T, P <: Position[P]](env : Environment[T, P], droneNode: DroneNode[T, P], override val unweightedVector : P) extends MotorSchema(env, droneNode, 1.0) {
  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = new Actuator(env, droneNode, velocityVector)
}
