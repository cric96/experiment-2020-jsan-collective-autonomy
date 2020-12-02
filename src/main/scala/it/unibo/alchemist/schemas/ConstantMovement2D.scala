package it.unibo.alchemist.schemas

import it.unibo.alchemist.actions.Actuator
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.{Action, Environment, Node, Reaction}
import it.unibo.alchemist.node.DroneNode

case class ConstantMovement2D[T](env : Environment[T, Euclidean2DPosition], node : DroneNode[T, Euclidean2DPosition], px : Double, py : Double, weight : Double)
  extends Actuator[T, Euclidean2DPosition](env, node, new Euclidean2DPosition(px, py)) {
  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = new ConstantMovement2D[T](env, node, px, py, weight)
}
