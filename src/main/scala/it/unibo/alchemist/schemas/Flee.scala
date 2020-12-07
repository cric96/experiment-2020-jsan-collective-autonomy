package it.unibo.alchemist.schemas

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.{Action, Environment, Node, Reaction}
import it.unibo.alchemist.node.DroneNode
import it.unibo.alchemist._
case class Flee[T](env : Environment[T, Euclidean2DPosition], node : DroneNode[T, Euclidean2DPosition], px : Double, py : Double, weight : Double)
  extends MotorSchema[T, Euclidean2DPosition](env, node, weight) {
  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = new Flee[T](env, node, px, py, weight : Double)
  private val direction = new Euclidean2DPosition(px, py)
  override def unweightedVector: Euclidean2DPosition = env.getPosition(node) - direction
}
