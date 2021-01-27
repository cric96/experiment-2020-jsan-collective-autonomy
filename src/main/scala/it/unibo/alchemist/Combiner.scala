package it.unibo.alchemist

import it.unibo.alchemist.model.implementations.actions.MotorSchema
import it.unibo.alchemist.model.interfaces.Position

trait Combiner[P <: Position[P]] {
  def combine(action: Seq[MotorSchema[_, P]]): P
}
class SumCombiner[T, P <: Position[P]] extends Combiner[P] {
  override def combine(action: Seq[MotorSchema[_, P]]): P = action.map(_.velocityVector).reduce(_ + _)
}
