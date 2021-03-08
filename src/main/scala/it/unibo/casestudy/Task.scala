package it.unibo.casestudy

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import it.unibo.casestudy.Task.Capability

/**
 * Describe a task to be executed by a node
 * @tparam P the aggregate interpreter type
 * @tparam O the result type
 */
trait Task[P <: AggregateProgram, O] {

  /**
   * @return the ID of whom creates the task
   */
  def source: ID

  def name: String

  /**
   * @return the capabilities needed to pursuit the task
   */
  def capabilities: Set[Capability]

  /**
   * exec the task using the aggregate interpreter given
   * @param po the interpreter
   * @return the result of the computation
   */
  def call(po: P): O = po.align(name)(name => behaviour(po)) //template method
  //TEMPLATE METHOD
  protected def behaviour(po: P): O //abstract method
}

object Task {

  /**
   * ADT that describes node capability
   */
  sealed trait Capability

  /**
   * The task could be executed by any node
   */
  case object Anyone extends Capability

  /**
   * The task could be pursuit with this specific capability
   * @param capability the capability name
   */
  case class Specific(capability: String) extends Capability

}
