package it.unibo.alchemist

import it.unibo.alchemist.model.implementations.actions.MotorSchema
import it.unibo.alchemist.model.implementations.nodes.MobileNode
import it.unibo.alchemist.model.interfaces.Position

trait Combiner[P <: Position[P]] {
  def combine(target : MobileNode[_, P], action : Seq[MotorSchema[_, P]]): P
}
class SumCombiner[T, P <: Position[P]] extends Combiner[P] {
  override def combine(target : MobileNode[_, P], action: Seq[MotorSchema[_, P]]): P = {
    action.map(_.velocityVector).reduce(_ + _)
  }
}
class DifferentialCombiner[T, P <: Position[P]] extends Combiner[P] {
  override def combine(target : MobileNode[_, P], action: Seq[MotorSchema[_, P]]): P = {
    target.velocity + action.map(_.velocityVector).reduce(_ + _)
  }
}
