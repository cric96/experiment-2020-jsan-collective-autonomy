package it.unibo.casestudy

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.ScafiAlchemistSupport
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import CollectiveTask._
import it.unibo.scafi.space.Point3D
//Example taken from https://www.sciencedirect.com/science/article/pii/S0167739X20304775
class FeedbackMutableArea extends AggregateProgram with Gradients
  with StandardSensors with FieldUtils with BlockT with BlockC with BlockS
  with BlockG with ScafiAlchemistSupport with ProcessDSL with StateManagement
  with CustomSpawn with TimeUtils {
  def grain : Double = 500 //TODO put as molecule?
  def alpha : Double = sense[Double]("alpha")
  override def main(): Any = {
    val leader = branch(isStationary) { S(grain, nbrRange) } { false }
    val dangerAnimal = SmartCollarBehaviour.dangerAnimalField(this)
    val save = SmartCollarBehaviour.anyHealer(this)
    rep(0.0) {
      influence => {
        val actualLeader = broadcastPenalized(leader, influence * 2, mid())
        val potential = distanceTo(mid() == actualLeader)
        val countHealer = countIn(potential, isStationary)
        val dangersCollected = C[Double, Map[ID, P]](potential,
          acc = (left, right) => SmartCollarBehaviour.combineDangerMap(left, right),
          dangerAnimal,
          Map.empty
        )
        val areaTask = dangersCollected.toSeq.sortBy(_._1)
          .headOption
          .map { case (id, p) => rescueTask(p, id) }
          .getOrElse(noTask())
        val myTask = broadcastPenalized(leader, influence * 2, areaTask)
        val taskExecution = myTask(this)
        node.put("target", taskExecution._1)
        node.put("targetId", taskExecution._2)
        val countExplorer = countIn(potential, isExplorer)
        node.put("sensed", dangersCollected)
        if(leader) {
          node.put("sensed", dangersCollected)
          node.put("influence", grain - (influence * 2))
          node.put("howMany", influence)
        }
        node.put("leader_id", actualLeader)
        mux(mutableAreaBehaviour) { exponentialBackOff(alpha, count = countHealer + countExplorer) } { 0 }
      }
    }
  }
  def isHealer : Boolean = sense[String]("type") == "healer"
  def isStationary : Boolean = sense[String]("type") == "stationary"
  def isStation : Boolean = node.get("station")
  def isExplorer : Boolean = sense[String]("type") == "explorer"
  def mutableAreaBehaviour : Boolean = sense[Double]("areaType").toInt == 0
  def exponentialBackOff(alpha : Double, count : Double) : Double = rep(count)(c => c * (1 - alpha) + alpha * count)
  def penalizedGradient(source: Boolean, penalization : Double): Double  = rep(Double.PositiveInfinity){
    d => mux(source){ penalization }{ minHoodPlus(nbr(d)+nbrRange()) }
  }
  def penalizedG[D](source : Boolean, penalization : Double)(field : D)(acc : D => D) : D = {
    val g = penalizedGradient(source, penalization)
    rep(field) { case value =>
      mux(source){ field }{ excludingSelf.minHoodSelector[Double,D](nbr{g}+nbrRange())(acc(nbr{value})).getOrElse(field) }
    }
  }
  def broadcastPenalized[D](source : Boolean, penalization : Double, data : D) : D = {
    penalizedG(source, penalization)(data)(d => d)
  }
  def countIn(potential: Double, field: Boolean): Int = C[Double, Int](potential, _ + _, mux(field) { 1 } { 0 }, 0)
  def rescueTask(targetPosition : P, who : ID) : Task[FeedbackMutableArea.Program, (Option[P], ID)] = {
    task[FeedbackMutableArea.Program, (Option[P], ID)](p => (Some(targetPosition), who))
      .where(p => p.sense[String]("type") == "healer")(p => (Option.empty[P], p.mid()))
  }
  def noTask() : Task[FeedbackMutableArea.Program, (Option[P], ID)] = task(p => (None, p.mid()))
  override def currentPosition(): Point3D = { //TODO fix alchemist - ScaFi incarnation
    val position = sense[Euclidean2DPosition](LSNS_POSITION)
    Point3D(position.getX, position.getY, 0.0)
  }
}

object FeedbackMutableArea {
  type Program = AggregateProgram with Gradients
    with StandardSensors with FieldUtils with BlockT with BlockC with BlockS
    with BlockG with ScafiAlchemistSupport with ProcessDSL with StateManagement
    with CustomSpawn with TimeUtils
}