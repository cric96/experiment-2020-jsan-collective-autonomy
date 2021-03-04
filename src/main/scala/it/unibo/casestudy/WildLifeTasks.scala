package it.unibo.casestudy

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import it.unibo.casestudy.Actuation.{Explore, Heal, NoActuation}
import it.unibo.casestudy.CollectiveTask.{Anyone, Capability, Specific}
import it.unibo.casestudy.FeedbackMutableArea.Program

object WildLifeTasks {
  val healer : Capability = Specific("healer")
  val explorer : Capability = Specific("explorer")
  abstract class CommonTask(val source : ID, val capabilities : Set[Capability]) extends CollectiveTask[Program, Actuation]
  case class HealTask(override val source : ID, targetId : ID, targetPosition : P) extends CommonTask(source, Set(healer)) {
    override def call(po: Program): Actuation = Heal(targetId, targetPosition)
  }
  case class ExploreTask(override val source : ID, center : P, radius : Double) extends CommonTask(source, Set(explorer)) {
    override def call(po: Program): Actuation = Explore(center, radius)
  }
  case class NoTask(override val source : ID) extends CommonTask(source, Set(Anyone)) {
    override def call(po: Program): Actuation = NoActuation
  }
}
