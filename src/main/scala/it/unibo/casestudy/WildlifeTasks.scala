package it.unibo.casestudy

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import it.unibo.casestudy.Actuation.{Explore, Heal, NoActuation}
import it.unibo.casestudy.Task.{Anyone, Capability, Specific}
import it.unibo.casestudy.WildlifeMonitoring.Program

/**
 * The tasks used to solve wildlife application problems
 */
object WildlifeTasks {

  val healer: Capability = Specific("healer")

  val explorer: Capability = Specific("explorer")

  /**
   * The root class of wild life tasks
   * @param source whom generates the task
   * @param capabilities needed for solving the task
   * @param name
   */
  abstract class CommonTask(val source: ID, val capabilities: Set[Capability], val name: String)
      extends Task[Program, Actuation] {}

  /**
   * Describe a task that has the goal to heal an animal
   * @param source whom generates the task
   * @param targetId one who needs to be healed
   * @param targetPosition where the node could be placed
   */
  case class HealTask(override val source: ID, targetId: ID, targetPosition: P)
      extends CommonTask(source, Set(healer), "heal") {

    override protected def behaviour(po: Program): Actuation = Heal(targetId, targetPosition)

  }

  /**
   * Describe an exploratory behaviour
   * @param source whom generates the task
   * @param center
   * @param radius
   */
  case class ExploreTask(override val source: ID, center: P, radius: Double)
      extends CommonTask(source, Set(explorer), "explore") {

    override protected def behaviour(po: Program): Actuation = Explore(center, radius)

  }

  /**
   * Describe an "empty" behaviour
   * @param source whom generates the task
   */
  case class NoTask(override val source: ID) extends CommonTask(source, Set(Anyone), "nothing") {

    override protected def behaviour(po: Program): Actuation = NoActuation

  }

}
