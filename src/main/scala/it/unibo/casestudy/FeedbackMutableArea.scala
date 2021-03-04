package it.unibo.casestudy

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.ScafiAlchemistSupport
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import CollectiveTask._
import it.unibo.scafi.space.Point3D

import scala.collection.immutable.Queue
//TODO
//Example taken from https://www.sciencedirect.com/science/article/pii/S0167739X20304775
class FeedbackMutableArea extends AggregateProgram with Gradients
  with StandardSensors with FieldUtils with RichBlockT with BlockC with BlockS
  with ScafiAlchemistSupport with ProcessDSL with StateManagement
  with CustomSpawn with TimeUtils with PenalizedG {
  lazy val grain : Double = sense[Double]("grain")
  lazy val alpha : Double = sense[Double]("alpha")
  lazy val movementWindow : Int = sense[Double]("movementWindow").toInt
  lazy val movementThr : Double = sense[Double]("movementThr")
  lazy val influenceFactor : Int = 4
  def isHealer : Boolean = sense[String]("type") == "healer"
  def isStationary : Boolean = sense[String]("type") == "stationary"
  def isExplorer : Boolean = sense[String]("type") == "explorer"
  override def main(): Any = {
    val leader = branch(isStationary) { S(grain, nbrRange) } { false }
    val dangerAnimal = SmartCollarBehaviour.dangerAnimalField(this)
    val save = SmartCollarBehaviour.anyHealer(this)
    rep(0.0) {
      influence => {
        val actualLeader = broadcastPenalized(leader, influence * influenceFactor, mid())
        val potential = distanceTo(mid() == actualLeader)
        val countHealer = countIn(potential, isStationary)
        val countExplorer = countIn(potential, isExplorer)
        val dangersCollected = C[Double, Map[ID, P]](potential,
          acc = (left, right) => SmartCollarBehaviour.combineDangerMap(left, right),
          dangerAnimal,
          Map.empty
        )
        val areaTask = dangersCollected.toSeq.sortBy(_._1)
          .headOption
          .map { case (id, p) => rescueTask(p, id) }
          .getOrElse(noTask())
        val myTask = broadcastPenalized(mid() == actualLeader, influence * influenceFactor, areaTask)
        val taskExecution = myTask(this)
        //Node actuation
        node.put("target", taskExecution._1)
        node.put("targetId", taskExecution._2)
        //Data exported
        node.put("taskReceived", taskExecution._1.nonEmpty)
        node.put("sensed", dangersCollected)
        node.put("leader_id", actualLeader)
        if(leader) {
          node.put("taskCreated", dangersCollected.size)
          node.put("sensed", dangersCollected)
          node.put("influence", grain - (influence * influenceFactor))
          node.put("howMany", influence)
        }
        mux(mutableAreaBehaviour) { exponentialBackOff(alpha, count = countHealer + countExplorer) } { 0 }
      }
    }
    //Capability sensing
    node.put("type", localCapabilitySensing())
  }
  override def currentPosition(): Point3D = { //TODO fix alchemist - ScaFi incarnation
    val position = sense[Euclidean2DPosition](LSNS_POSITION)
    Point3D(position.getX, position.getY, 0.0)
  }
  def localCapabilitySensing() : String = {
    val trajectory = recentValues(movementWindow, currentPosition())
    val distance = trajectory.tail.zip(trajectory.dropRight(1))
      .map { case (a, b) => a.distance(b) }
      .sum
    mux(isHealer) { "healer" } { typeFromDistance(distance) } //local behaviour influence the global structure
  }
  def mutableAreaBehaviour : Boolean = sense[Double]("areaType").toInt == 0
  def exponentialBackOff(alpha : Double, count : Double) : Double = rep(count)(c => c * (1 - alpha) + alpha * count)
  def countIn(potential: Double, field: Boolean): Int = C[Double, Int](potential, _ + _, mux(field) { 1 } { 0 }, 0)
  def rescueTask(targetPosition : P, who : ID) : Task[FeedbackMutableArea.Program, (Option[P], ID)] = {
    task[FeedbackMutableArea.Program, (Option[P], ID)](p => (Some(targetPosition), who))
      .where(p => p.sense[String]("type") == "healer")(p => (Option.empty[P], p.mid()))
  }
  def noTask() : Task[FeedbackMutableArea.Program, (Option[P], ID)] = task(p => (None, p.mid()))
  def typeFromDistance(distance : Double) : String = if(distance.toInt <= movementThr) {
    "stationary"
  } else {
    "explorer"
  }
}

object FeedbackMutableArea {
  type Program = AggregateProgram with Gradients
    with StandardSensors with FieldUtils with BlockT with BlockC with BlockS
    with BlockG with ScafiAlchemistSupport with ProcessDSL with StateManagement
    with CustomSpawn with TimeUtils
}