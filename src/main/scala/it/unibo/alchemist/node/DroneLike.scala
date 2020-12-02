package it.unibo.alchemist.node

import it.unibo.alchemist.model.interfaces.Position

trait DroneLike[P <: Position[P]] {
  def speed : Double
  def currentVector : P
}
