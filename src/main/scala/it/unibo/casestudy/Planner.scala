package it.unibo.casestudy
import it.unibo.alchemist.model.implementations.nodes.{ NodeManager, SimpleNodeManager }
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import it.unibo.casestudy.CollectiveTask.{ Anyone, Capability }
import it.unibo.casestudy.WildlifeMonitoring.Program
import it.unibo.casestudy.WildlifeTasks.NoTask

import scala.util.Random

case class Planner(random: Random, manager: NodeManager) {
  def eval(
    myId: ID,
    leaderId: ID,
    capabilities: Set[Capability],
    tasks: Seq[CollectiveTask[Program, Actuation]],
    collectiveThr: Double
  ): CollectiveTask[Program, Actuation] = {
    val collective = random.nextDouble() <= collectiveThr
    //val collective = collectiveThr.toInt == 0
    manager.put("tasks", tasks.filter(_.source == leaderId))
    tasks
      .filter(a => a.source == (if (collective) leaderId else myId))
      .find(required =>
        capabilities.intersect(required.capabilities) == capabilities || required.capabilities.contains(Anyone)
      )
      .getOrElse(NoTask(leaderId))
  }
}
