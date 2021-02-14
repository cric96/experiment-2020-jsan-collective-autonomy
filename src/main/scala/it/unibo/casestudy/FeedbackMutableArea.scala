package it.unibo.casestudy

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.ScafiAlchemistSupport
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._

class FeedbackMutableArea extends AggregateProgram with Gradients
  with StandardSensors with FieldUtils with BlockT with BlockC with BlockS
  with BlockG with ScafiAlchemistSupport with ProcessDSL with StateManagement
  with CustomSpawn with TimeUtils {
  def grain : Double = 500 //TODO put as molecule?
  override def main(): Any = {
    val leader = branch(isStationary) { S(grain, nbrRange) } { false }
    rep(0.0) {
      influence => {
        val actualLeader = broadcastPenalized(leader, influence, mid())
        val potential = distanceTo(mid() == actualLeader)
        val countHealer = countIn(potential, isStationary)
        val countExploratory = countIn(potential, isExploratory)
        if(leader) {
          node.put("area", grain - (influence * 2))
          node.put("howMany", influence)
        }
        node.put("leader_id", actualLeader)
        exponentialBackOff(alpha = 0.1, count = countHealer + countExploratory)
      }
    }
  }
  def isHealer : Boolean = sense[String]("type") == "healer"
  def isStationary : Boolean = sense[String]("type") == "stationary"
  def isExploratory : Boolean = sense[String]("type") == "exploratory"
  def exponentialBackOff(alpha : Double, count : Double) : Double = rep(count)(c => c * (1 - alpha) + alpha * count)
  def penalizedGradient(source: Boolean, penalization : Double): Double  = rep(Double.PositiveInfinity){
    d => mux(source){ penalization }{ minHoodPlus(nbr(d)+nbrRange()) }
  }
  def penalizedG[D](source : Boolean, penalization : Double)(field : D)(acc : D => D) : D = {
    val g = penalizedGradient(source, penalization)
    rep(field) { case (value) =>
      mux(source){ field }{ excludingSelf.minHoodSelector[Double,D](nbr{g}+nbrRange())(acc(nbr{value})).getOrElse(field) }
    }
  }
  def broadcastPenalized[D](source : Boolean, penalization : Double, data : D) : D = penalizedG(source, penalization)(data)(d => d)
  def countIn(potential: Double, field: Boolean): Int = C[Double, Int](potential, _ + _, mux(field) { 1 } { 0 }, 0)
}
