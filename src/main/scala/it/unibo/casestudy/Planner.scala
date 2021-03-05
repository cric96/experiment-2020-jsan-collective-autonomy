package it.unibo.casestudy
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import it.unibo.casestudy.CollectiveTask.{Anyone, Capability}
import it.unibo.casestudy.WildlifeMonitoring.Program
import it.unibo.casestudy.WildlifeTasks.NoTask

object Planner {
  def eval(myId : ID, leaderId : ID, capabilities : Set[Capability], tasks : Seq[CollectiveTask[Program, Actuation]], collective : Boolean) : CollectiveTask[Program, Actuation] = {
    tasks
      .filter(a => a.source == (if(collective) { leaderId } else { myId }))
      .find(required => capabilities.intersect(required.capabilities) == capabilities || required.capabilities.contains(Anyone))
      .getOrElse(NoTask(leaderId))
  }
}
