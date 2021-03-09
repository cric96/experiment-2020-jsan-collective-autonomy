package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist._
import it.unibo.alchemist.model.implementations.nodes.MobileNode
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.{Action, Environment, Node, Reaction}

/**
 * MOTOR SCHEMA BEHAVIOUR
 * move the node far from a position.
 *
 * @param px
 * @param py
 */
case class FleeSchema[T](
  env: Environment[T, Euclidean2DPosition],
  node: MobileNode[T, Euclidean2DPosition],
  px: Double,
  py: Double,
  weight: Double
) extends MotorSchema[T, Euclidean2DPosition](env, node, weight) {

  private val direction = new Euclidean2DPosition(px, py)

  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = new FleeSchema[T](env, node, px, py, weight: Double)

  override def unweightedVector: Euclidean2DPosition = env.getPosition(node) - direction

}
