package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.interfaces.{ Environment, Position }

/**
 * @param env
 * @tparam T
 * @tparam P
 */
abstract class MobileNode[T, P <: Position[P]](env: Environment[T, P]) extends ScafiNode[T, P](env) {

  def maximumSpeed: Double

  def group: String

  def velocity: P

  def setVector(v: P): Unit

}
