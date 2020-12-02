package it.unibo.alchemist.schemas

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.{Action, Environment, Node, Reaction}
import it.unibo.alchemist.node.DroneNode
import it.unibo.alchemist._
case class Alignment[T](env : Environment[T, Euclidean2DPosition], node : DroneNode[T, Euclidean2DPosition], weight : Double)
  extends MotorSchema[T, Euclidean2DPosition](env, node, weight) {
  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = new Alignment[T](env, node, weight)

  override def unweightedVector: Euclidean2DPosition = {
    val neighbourhood = getNeighbourOf(node)
    (if (neighbourhood.isEmpty) {
      env.origin
    } else {
      val avgVelocity = neighbourhood.map(_.currentVector).foldLeft(env.origin)(_ + _) / neighbourhood.size
      avgVelocity
    } - node.currentVector)
  }
}