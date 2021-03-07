package it.unibo.alchemist

import it.unibo.alchemist.model.implementations.actions.MotorSchema
import it.unibo.alchemist.model.implementations.nodes.MobileNode
import it.unibo.alchemist.model.interfaces.Position

/**
 * Combine a sequence of motor schema actions into a single velocity (as in mother schema control architecture)
 */
trait Combiner[P <: Position[P]] {

  /**
   * the combination logic
   *
   * @param target  the node in which the new velocity will be given
   * @param actions the actions that will be processed
   * @return a velocity vector following the combination logic.
   */
  def combine(target: MobileNode[_, P], actions: Seq[MotorSchema[_, P]]): P

}

/**
 * Produce a velocity as the sum of all behaviours passed
 */
class SumCombiner[T, P <: Position[P]] extends Combiner[P] {

  override def combine(target: MobileNode[_, P], actions: Seq[MotorSchema[_, P]]): P =
    actions.map(_.velocityVector).reduce(_ + _)

}

/**
 * Produce a velocity as the sum of all behaviours passed plus the old velocity of node (simulating a differential behaviour).
 */
class DifferentialCombiner[T, P <: Position[P]] extends Combiner[P] {

  override def combine(target: MobileNode[_, P], actions: Seq[MotorSchema[_, P]]): P =
    target.velocity + actions.map(_.velocityVector).reduce(_ + _)

}
