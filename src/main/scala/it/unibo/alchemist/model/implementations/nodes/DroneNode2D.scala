package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
class DroneNode2D[T](env : Environment[T, Euclidean2DPosition], override val speed : Double = 1.0) extends DroneNode[T, Euclidean2DPosition](env) {
  private var vector : Euclidean2DPosition = new Euclidean2DPosition(0.0, 0.0)
  override def setVector(v: Euclidean2DPosition): Unit = vector = v
  override def currentVector: Euclidean2DPosition = vector
}
