package it.unibo.alchemist.node
import it.unibo.alchemist.model.implementations.nodes.ScafiNode
import it.unibo.alchemist.model.interfaces.{Environment, Position}

class FireNode[T, P <: Position[P]](env : Environment[T, P]) extends ScafiNode[T, P](env) {
  def intensity(p : P) : Double = { 10.0 } //TODO
  var area : Double = 10.0 //TODO
}
