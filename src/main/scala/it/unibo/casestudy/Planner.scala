package it.unibo.casestudy
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import it.unibo.casestudy.CollectiveTask.{Anyone, Capability}
import it.unibo.casestudy.FeedbackMutableArea.Program
import it.unibo.casestudy.WildLifeTasks.NoTask

object Planner {
  def eval(leaderId : ID, capabilities : Set[Capability], tasks : Seq[CollectiveTask[Program, Actuation]]) : CollectiveTask[Program, Actuation] = {
    tasks.filter(_.source == leaderId)
      .find(required => capabilities.intersect(required.capabilities) == capabilities || required.capabilities.contains(Anyone))
      .getOrElse(NoTask(leaderId))
  }
}
