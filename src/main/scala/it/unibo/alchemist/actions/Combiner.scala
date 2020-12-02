package it.unibo.alchemist.actions

import it.unibo.alchemist._
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.schemas.MotorSchema

trait Combiner[P <: Position[P]] {
  def combine(action: Seq[MotorSchema[_, P]]): P
}
class SumCombiner[T, P <: Position[P]] extends Combiner[P] {
  override def combine(action: Seq[MotorSchema[_, P]]): P = action.map(_.velocityVector).reduce(_ + _)
}
