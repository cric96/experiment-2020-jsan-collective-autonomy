package it.unibo.casestudy

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.{ScafiAlchemistSupport, _}
import it.unibo.casestudy.CollectiveTask._
import it.unibo.casestudy.WildlifeMonitoring.Program
import it.unibo.casestudy.WildlifeTasks.{ExploreTask, HealTask, NoTask}
import it.unibo.scafi.space.Point3D
//TODO
//Example taken from https://www.sciencedirect.com/science/article/pii/S0167739X20304775
class WildlifeMonitoring extends AggregateProgram with Gradients
  with StandardSensors with FieldUtils with RichStateManagement with BlockC with BlockS with BlockG
  with ScafiAlchemistSupport with ProcessDSL with StateManagement with SmartCollarBehaviour
  with CustomSpawn with TimeUtils with PenalizedG {
  lazy val grain : Double = sense[Double]("grain")
  lazy val alpha : Double = sense[Double]("alpha")
  lazy val movementWindow : Int = sense[Double]("movementWindow").toInt
  lazy val movementThr : Double = sense[Double]("movementThr")
  lazy val influenceFactor : Double = 2
  def isHealer : Boolean = sense[String]("type") == "healer"
  def isStationary : Boolean = sense[String]("type") == "stationary"
  def isExplorer : Boolean = sense[String]("type") == "explorer"
  def isAnimal : Boolean = !node.has("type")
  override def main(): Any = {
    val dangerAnimal = animalBehaviour()
    branch(!isAnimal) { rescueBehaviour(dangerAnimal) } { }
  }
  def animalBehaviour(): Map[ID, P] = {
    val dangerAnimal = dangerAnimalField()
    val save = canHealAnimal()
    val status = node.get[Boolean]("danger")
    branch(isAnimal) {
      val (danger, count) = rep(false, 0) {
        case (_, count) => (mux(save && status) { (false, count + 1) } { (status, count) })
      }
      node.put("healCount", count)
      node.put("danger", danger)
    } { }
    dangerAnimal
  }
  def rescueBehaviour(dangerAnimal : Map[ID, P]) = {
    val leader = branch(isStationary) { S(grain, nbrRange) } { false }
    rep(0.0) {
      influence => {
        val influencePenalization = influence * influenceFactor
        val actualLeader = broadcastPenalized(leader, influencePenalization, mid())
        align(actualLeader) { actualLeader => {
          val sourceArea = mid() == actualLeader
          val potential = distanceTo(sourceArea)
          val countHealer = countIn(potential, isStationary)
          val countExplorer = countIn(potential, isExplorer)
          val dangersCollected = C[Double, Map[ID, P]](potential,
            acc = (left, right) => combineDangerMap(left, right),
            dangerAnimal,
            Map.empty
          )
          val dangersInArea = broadcastPenalized(sourceArea, influencePenalization, dangersCollected)
          val localTask = dangersInArea.toSeq.sortBy { case (id, p) => p.distance(currentPosition()) }
            .headOption
            .map { case (id, p) => HealTask(mid(), id, p) }
            .getOrElse(NoTask(mid()))
          val areaTask : CollectiveTask[Program, Actuation] = dangersCollected.toSeq.sortBy { case (id, p) => id }
            .headOption
            .map { case (id, p) => HealTask(mid(), id, p) }
            .getOrElse(NoTask(mid()))
          //val exploreTask = broadcastPenalized(sourceArea, influencePenalization, ExploreTask(mid(), currentPosition(), grain))
          val healTask : CollectiveTask[Program, Actuation] = broadcastPenalized(sourceArea, influencePenalization, areaTask)
          val selectedTask = Planner.eval(mid(), actualLeader, capability, Seq(healTask, localTask), collective)
          val actuation = selectedTask.call(this)
          Actuator.act(node, actuation)
          //Data exported
          node.put("taskReceived", !selectedTask.isInstanceOf[NoTask])
          node.put("sensed", dangersCollected)
          node.put("leader_id", actualLeader)
          if(leader) {
            node.put("taskCreated", dangersCollected.size)
            node.put("sensed", dangersCollected)
            node.put("influence", grain - (influencePenalization))
            node.put("howMany", countHealer + countExplorer)
          }
          mux(mutableArea) { exponentialBackOff(alpha, count = countHealer + countExplorer) } { 0 }
        }}

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
  def capability : Set[Capability] = Set(Specific(sense[String]("type")))
  def mutableArea : Boolean = sense[Double]("areaType").toInt == 0
  def collective : Boolean = sense[Double]("behaviourType").toInt == 0
  def exponentialBackOff(alpha : Double, count : Double) : Double = rep(count)(c => c * (1 - alpha) + alpha * count)
  def countIn(potential: Double, field: Boolean): Int = C[Double, Int](potential, _ + _, mux(field) { 1 } { 0 }, 0)
  def typeFromDistance(distance : Double) : String = if(distance.toInt <= movementThr) {
    "stationary"
  } else {
    "explorer"
  }
}

object WildlifeMonitoring {
  type Program = AggregateProgram with Gradients
    with StandardSensors with FieldUtils with BlockT with BlockC with BlockS
    with BlockG with ScafiAlchemistSupport with ProcessDSL with StateManagement
    with CustomSpawn with TimeUtils
}