package it.unibo.casestudy

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.ScafiAlchemistSupport
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._

class FeedbackMutableArea extends AggregateProgram with Gradients
  with StandardSensors with FieldUtils with BlockT with BlockC with BlockS
  with BlockG with ScafiAlchemistSupport with ProcessDSL with StateManagement
  with CustomSpawn with TimeUtils {
  def isHealer : Boolean = sense[String]("type") == "healer"
  def isStationary : Boolean = sense[String]("type") == "stationary"
  def isExploratory : Boolean = sense[String]("type") == "exploratory"
  def grain : Double = 500 //todo put in a better place
  def countIn(potential: Double, field: Boolean): Int = {
    node.put("local", branch(field && !potential.isInfinity) { 1 } { 0 })
    C[Double, Int](potential, _ + _, branch(field && !potential.isInfinity) { 1 } { 0 }, 0)
  }
  override def main(): Any = {
    val leader = branch(isStationary) { S(grain, nbrRange) } { false }
    rep(0.0) {
      influence => {
        val potential = G[Double](leader, influence, v => v + nbrRange(), nbrRange)
        val countHealer = countIn(potential, isHealer)
        val countExploratory = countIn(potential, isExploratory)
        countHealer + countExploratory
      }
    }
  }
}
