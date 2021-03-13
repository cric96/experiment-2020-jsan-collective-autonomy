package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist._
import it.unibo.alchemist.model.implementations.nodes.MobileNode
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.{ Action, Environment, Node, Reaction }

/**
 * MOTOR SCHEMA BEHAVIOUR
 * Follow the cohesion logic of Craig Reynolds (https://www.red3d.com/cwr/boids/
 */
case class Cohesion[T](
  env: Environment[T, Euclidean2DPosition],
  node: MobileNode[T, Euclidean2DPosition],
  weight: Double
) extends MotorSchema[T, Euclidean2DPosition](env, node, weight) {

  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = new Cohesion[T](env, node, weight)

  override def unweightedVector: Euclidean2DPosition = {
    val droneNeighbour = getNeighbourOf(node)
    val centroid =
      if (droneNeighbour.isEmpty)
        env.origin
      else {
        val neighbourPositionSum = droneNeighbour.map(node => env.getPosition(node)).foldLeft(env.origin)(_ + _)
        neighbourPositionSum / droneNeighbour.size
      }
    centroid - env.getPosition(node)
  }

}
