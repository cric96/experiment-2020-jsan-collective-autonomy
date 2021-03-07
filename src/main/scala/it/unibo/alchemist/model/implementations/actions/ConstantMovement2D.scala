package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.nodes.MobileNode
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.{ Action, Environment, Node, Reaction }

/**
 * MOTOR SCHEMA BEHAVIOUR
 * Describe a constant velocity movement.
 * @param vx velocity component x
 * @param vy velocity component y
 */
case class ConstantMovement2D[T](
  env: Environment[T, Euclidean2DPosition],
  node: MobileNode[T, Euclidean2DPosition],
  vx: Double,
  vy: Double,
  weight: Double
) extends MotorSchema[T, Euclidean2DPosition](env, node, weight) {
  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = new ConstantMovement2D[T](env, node, vx, vy, weight)
  override protected def unweightedVector: Euclidean2DPosition = env.makePosition(vx, vy)
}
