package it.unibo.alchemist.schemas

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.{Action, Environment, Node, Reaction}
import it.unibo.alchemist.node.DroneNode
import it.unibo.alchemist._
case class Separation[T](env : Environment[T, Euclidean2DPosition], node : DroneNode[T, Euclidean2DPosition], separationDistance : Double, weight : Double)
  extends MotorSchema[T, Euclidean2DPosition](env, node, weight) {
  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = new Separation[T](env, node, separationDistance, weight)
  override def unweightedVector: Euclidean2DPosition = {
    val neighborhood = getNeighbourOf(node)
    val nodePosition = env.getPosition(node)
    neighborhood.map(env.getPosition(_))
      .filter(node => node.getDistanceTo(nodePosition) < separationDistance)
      .foldLeft(env.origin)((acc, point) => acc - (point - nodePosition))
  }
}