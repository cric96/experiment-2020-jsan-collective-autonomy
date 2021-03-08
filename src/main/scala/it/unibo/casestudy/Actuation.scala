package it.unibo.casestudy

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._

/**
 * An ADT that describes the possible actuation in this application
 */
sealed trait Actuation

object Actuation {

  /** no actuation will be performed */
  case object NoActuation extends Actuation

  /**
   * try to heal an animal
   * @param target the id of the animal in danger
   * @param targetPosition the position of the animal in danger
   */
  case class Heal(target: ID, targetPosition: P) extends Actuation

  /**
   * explore an area
   * @param center the zone center
   * @param radius the exploration radius
   */
  case class Explore(center: P, radius: Double) extends Actuation

}
