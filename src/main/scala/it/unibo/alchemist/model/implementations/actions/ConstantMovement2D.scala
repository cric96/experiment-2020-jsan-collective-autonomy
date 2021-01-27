package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.nodes.MobileNode
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.{Action, Environment, Node, Reaction}

case class ConstantMovement2D[T](env: Environment[T, Euclidean2DPosition], node: MobileNode[T, Euclidean2DPosition], px: Double, py: Double, weight: Double)
  extends Actuator[T, Euclidean2DPosition](env, node, new Euclidean2DPosition(px, py)) {
  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = new ConstantMovement2D[T](env, node, px, py, weight)
}
