package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.interfaces.Position

trait DroneLike[P <: Position[P]] {
  def speed: Double

  def currentVector: P
}
