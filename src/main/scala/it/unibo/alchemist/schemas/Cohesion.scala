package it.unibo.alchemist.schemas

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.{Action, Environment, Node, Reaction}
import it.unibo.alchemist.node.DroneNode
import it.unibo.alchemist._
case class Cohesion[T](env : Environment[T, Euclidean2DPosition], node : DroneNode[T, Euclidean2DPosition], weight : Double)
  extends MotorSchema[T, Euclidean2DPosition](env, node, weight) {
  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = new Cohesion[T](env, node, weight)
  override def unweightedVector: Euclidean2DPosition = {
    val droneNeighbour = getNeighbourOf(node)
    val centroid = if(droneNeighbour.isEmpty) {
      env.origin
    } else {
      val neighbourPositionSum = droneNeighbour.map(node => env.getPosition(node)).foldLeft(env.origin)(_ + _)
      neighbourPositionSum / droneNeighbour.size
    }
    centroid - env.getPosition(node)
  }
}
