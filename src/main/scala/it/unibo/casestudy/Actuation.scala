package it.unibo.casestudy
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._

sealed trait Actuation
object Actuation {
  case object NoActuation extends Actuation
  case class Heal(target: ID, targetPosition: P) extends Actuation
  case class Explore(center: P, radius: Double) extends Actuation
}
