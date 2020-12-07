package it.unibo.alchemist.node
import it.unibo.alchemist.model.implementations.nodes.ScafiNode
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment

class FireNode2D[T](env : Environment[T, Euclidean2DPosition]) extends ScafiNode[T, Euclidean2DPosition](env) {
  def intensity(p : Euclidean2DPosition) : Double = { 10.0 }
}
