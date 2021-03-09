package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.interfaces.{Environment, Position}

/**
 * A node that could change its position in time
 */
abstract class MobileNode[T, P <: Position[P]](env: Environment[T, P]) extends ScafiNode[T, P](env) {

  /**
   * the maximum limit of the velocity module.
   */
  def maximumSpeed: Double

  /**
   * the group in which the node belong
   */
  def group: String

  /**
   * the current velocity applied to this node
   */
  def velocity: P

  /**
   * change the node velocity
   */
  def setVector(v: P): Unit

}
