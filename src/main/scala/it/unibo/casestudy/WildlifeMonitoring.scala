package it.unibo.casestudy

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.{ ScafiAlchemistSupport, _ }
import it.unibo.casestudy.CollectiveTask._
import it.unibo.casestudy.WildlifeMonitoring.Program
import it.unibo.casestudy.WildlifeTasks.{ ExploreTask, HealTask, NoTask }
import it.unibo.scafi.space.Point3D
//TODO
//Example taken from https://www.sciencedirect.com/science/article/pii/S0167739X20304775
class WildlifeMonitoring
    extends AggregateProgram
    with Gradients
    with StandardSensors
    with FieldUtils
    with RichStateManagement
    with BlockC
    with BlockS
    with BlockG
    with ScafiAlchemistSupport
    with ProcessDSL
    with StateManagement
    with SmartCollarBehaviour
    with CustomSpawn
    with TimeUtils
    with PenalizedG {
  lazy val grain: Double = sense[Double]("grain")
  lazy val alpha: Double = sense[Double]("alpha")
  lazy val movementWindow: Int = sense[Double]("movementWindow").toInt
  lazy val movementThr: Double = sense[Double]("movementThr")
  lazy val influenceFactor: Double = 2
  lazy val planner = Planner(randomGen, node)
  def isHealer: Boolean = sense[String]("type") == "healer"
  def isStationary: Boolean = sense[String]("type") == "stationary"
  def isExplorer: Boolean = sense[String]("type") == "explorer"
  def isAnimal: Boolean = !node.has("type")

  override def nbrRange(): Double = super.nbrRange() + 20 //in FGCS there is similar instruction, why?
  override def main(): Any = {
    val dangerAnimal = animalBehaviour()
    val trajectory = recentValues(5, currentPosition())
    node.put("trajectory", trajectory.last - trajectory.head)
    branch(!isAnimal)(rescueBehaviour(dangerAnimal)) {}
  }
  def animalBehaviour(): Map[ID, P] = {
    val dangerAnimal = dangerAnimalField()
    val save = canHealAnimal()
    val status = node.get[Boolean]("danger")
    branch(isAnimal) {
      val (danger, count) = rep(false, 0) {
        case (_, count) => (mux(save && status)((false, count + 1)) { (status, count) })
      }
      node.put("healCount", count)
      node.put("danger", danger)
    } {}
    dangerAnimal
  }
  def rescueBehaviour(dangerAnimal: Map[ID, P]) = {
    val leader = branch(isStationary)(S(grain, nbrRange))(false)
    rep(0.0) { influence =>
      val influencePenalization = influence * influenceFactor
      val actualLeader = broadcastPenalized(leader, influencePenalization, mid())
      align(actualLeader) { actualLeader =>
        val sourceArea = mid() == actualLeader
        val potential = distanceTo(sourceArea)
        val countHealer = countIn(potential, isStationary)
        val countExplorer = countIn(potential, isExplorer)
        val dangersCollected = C[Double, Map[ID, P]](
          potential,
          acc = (left, right) => combineDangerMap(left, right),
          dangerAnimal,
          Map.empty
        )
        //val dangersInArea = G[Map[ID, P]](sourceArea, dangersCollected, v => v, nbrRange)
        val localTask = dangerAnimal.toSeq.sortBy { case (id, p) => p.distance(currentPosition()) }.headOption.map {
          case (id, p) => HealTask(mid(), id, p)
        }
        val areaTask = dangersCollected.toSeq.sortBy { case (id, p) => p.distance(currentPosition()) }.headOption.map {
          case (id, p) => HealTask(mid(), id, p)
        }
        val exploreTask = ExploreTask(mid(), currentPosition(), grain - (influence))
        val exploreTaskArea = Some(broadcastPenalized(sourceArea, influencePenalization, exploreTask))
          .filterNot(_.source == mid())
        val healTask = G[Option[CollectiveTask[Program, Actuation]]](sourceArea, areaTask, v => v, nbrRange)
        val tasks = Seq(healTask, localTask, exploreTaskArea).collect { case Some(task) => task }
        val selectedTask = planner.eval(mid(), actualLeader, capability, tasks, collective)
        val actuation = selectedTask.call(this)
        node.put("explore", exploreTask)
        Actuator.act(node, actuation)
        //Data exported
        node.put("task", selectedTask)
        node.put("taskReceived", !selectedTask.isInstanceOf[NoTask])
        node.put("sensed", dangersCollected)
        node.put("leader_id", actualLeader)
        if (leader) {
          node.put("taskCreated", dangersCollected.size)
          node.put("sensed", dangersCollected)
          node.put("influence", grain - (influencePenalization))
          node.put("howMany", countHealer + countExplorer)
        }
        mux(mutableArea)(exponentialBackOff(alpha, count = countHealer + countExplorer))(0)
      }

    }
    //Capability sensing
    node.put("type", localCapabilitySensing())
  }
  override def currentPosition(): Point3D = { //TODO fix alchemist - ScaFi incarnation
    val position = sense[Euclidean2DPosition](LSNS_POSITION)
    Point3D(position.getX, position.getY, 0.0)
  }
  def localCapabilitySensing(): String = {
    val trajectory = recentValues(movementWindow, currentPosition())
    val distance = trajectory.tail.zip(trajectory.dropRight(1)).map { case (a, b) => a.distance(b) }.sum
    mux(isHealer)("healer")(typeFromDistance(distance)) //local behaviour influence the global structure
  }
  def capability: Set[Capability] = Set(Specific(sense[String]("type")))
  def mutableArea: Boolean = sense[Double]("areaType").toInt == 0
  def collective: Double = sense[Double]("behaviourType")
  def exponentialBackOff(alpha: Double, count: Double): Double = rep(count)(c => c * (1 - alpha) + alpha * count)
  def countIn(potential: Double, field: Boolean): Int = C[Double, Int](potential, _ + _, mux(field)(1)(0), 0)
  def typeFromDistance(distance: Double): String =
    if (distance.toInt <= movementThr)
      "stationary"
    else
      "explorer"
}

object WildlifeMonitoring {
  type Program = AggregateProgram
    with Gradients
    with StandardSensors
    with FieldUtils
    with BlockT
    with BlockC
    with BlockS
    with BlockG
    with ScafiAlchemistSupport
    with ProcessDSL
    with StateManagement
    with CustomSpawn
    with TimeUtils
}
