package it.unibo.casestudy

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import it.unibo.casestudy.CollectiveTask.Capability
//TODO

trait CollectiveTask[P <: AggregateProgram, O] {

  def source: ID

  def name: String

  def capabilities: Set[Capability]

  def call(po: P): O = po.align(name)(name => behaviour(po)) //template method
  protected def behaviour(po: P): O //abstract method
}

object CollectiveTask {

  sealed trait Capability

  case object Anyone extends Capability

  case class Specific(capability: String) extends Capability

}
