package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.nodes.MobileNode
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.{ Action, Environment, Node, Reaction }
import it.unibo.alchemist._

/**
 * MOTOR SCHEMA BEHAVIOUR
 * Follow the alignment logic of Craig Reynolds (https://www.red3d.com/cwr/boids/)
 */
case class Alignment[T](
  env: Environment[T, Euclidean2DPosition],
  node: MobileNode[T, Euclidean2DPosition],
  weight: Double
) extends MotorSchema[T, Euclidean2DPosition](env, node, weight) {

  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = new Alignment[T](env, node, weight)

  override def unweightedVector: Euclidean2DPosition = {
    val neighbourhood = getNeighbourOf(node)
    if (neighbourhood.isEmpty)
      env.origin
    else {
      val avgVelocity = neighbourhood.map(_.velocity).foldLeft(env.origin)(_ + _) / neighbourhood.size
      avgVelocity - node.velocity
    }
  }

}
