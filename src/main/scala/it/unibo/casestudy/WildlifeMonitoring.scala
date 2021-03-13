package it.unibo.casestudy

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.{ScafiAlchemistSupport, _}
import it.unibo.casestudy.Task._
import it.unibo.casestudy.WildlifeMonitoring.Program
import it.unibo.casestudy.WildlifeTasks.{ExploreTask, HealTask}
import it.unibo.scafi.space.Point3D

//Example inspired by https://www.sciencedirect.com/science/article/pii/S0167739X20304775
/**
 * The aggregate program for the wildlife monitoring application.
 * There is three nodes type:
 *  - animal: a node that needs to be rescued if it is in danger. The danger status is domain-specific. It could be a sort of "smart collar";
 *  - mobile node:  a node that moves around the world and could have the capability to heal an animal;
 *  - station: a fixed node that works as a gateway for mobile nodes;
 *
 * At runtime, the program identifies nodes capability (healer, explorer, stationary) according to the local behaviour sensed.
 * In general, to rescue an animal, we need n healer (n is a domain parameter).
 * The program follows the SCR pattern:
 *  - a leader election is made in the stationary nodes (via S);
 *  - mobile node sense the animal in danger and send the local information to the leader (via C);
 *  - the leader chooses what is the animal that needs to be rescued;
 *  - the leader shares its choose (via G)
 * Each agent has some level of local autonomy (e.g. explore the world, avoid other nodes) and the program tries to coordinate the "aggregate" to pursuit the application goal.
 * We identify two scenarios for the collective autonomy aspect:
 *    - "structural" scenario: The leaders' influence changes according to the type of nodes inside its area. So the collective structure change according to local/global choices.
 *    - "local/collective" scenario: Agents have a probability of not listen to the collective choose and could act selfishly (e.g. trying to heal the nearest animal). This
 *    choice is regulated by a probability p. We check how the behaviour of collective change according to the parameter p.
 */
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
    with SmartCollarUtils
    with CustomSpawn
    with TimeUtils
    with PenalizedG {

  lazy val grain: Double = sense[Double]("grain")

  lazy val alpha: Double = sense[Double]("alpha")

  lazy val movementWindow: Int = sense[Double]("movementWindow").toInt

  lazy val movementThr: Double = sense[Double]("movementThr")

  lazy val influenceFactor: Double = 2

  lazy val planner = Planner(randomGen)

  override def nbrRange(): Double = super.nbrRange() + 20 //ad a penality of each hop

  override def main(): Any = {
    val targets = animalInDanger()
    branch(!isAnimal)(rescue(targets)) {}
  }

  //check if an animal could be rescue and compute the animal in danger map (id -> position)
  private def animalInDanger(): Map[ID, P] = {
    val dangerAnimal = dangerAnimalField()
    val save = canHealAnimal()
    val status = node.get[Boolean]("danger")
    val (danger, count) = branch(isAnimal) {
      rep(false, 0) {
        case (_, count) => mux(save && status)((false, count + 1))((status, count))
      }
    }((false, 0))
    if (isAnimal) { // non aggregate code
      node.put("healCount", count)
      node.put("danger", danger)
    }
    dangerAnimal
  }

  private def rescue(dangerAnimal: Map[ID, P]): Unit = {
    val leader = branch(isStationary)(S(grain, nbrRange))(false)
    rep(0.0) { influence =>
      val influencePenalization = influence * influenceFactor
      val actualLeader = broadcastPenalized(leader, influencePenalization, mid())
      //align computation on leader id, creating non overlapping zones
      align(actualLeader) { actualLeader =>
        val sourceArea = mid() == actualLeader
        val potential = distanceTo(sourceArea)
        //mutable area section
        val (healer, explorer): (Int, Int) = branch(mutableArea) {
          (countIn(potential, isHealer), countIn(potential, isExplorer))
        } {
          (0, 0)
        }
        val dangersCollected = C[Double, Map[ID, P]](
          potential,
          acc = (left, right) => combineDangerMap(left, right),
          dangerAnimal,
          Map.empty
        )
        //val dangersInArea = G[Map[ID, P]](sourceArea, dangersCollected, v => v, nbrRange)
        //The agent is myopic, choose to heal the nearest animal
        val localHealTask = dangerAnimal.toSeq.sortBy { case (_, p) => p.distance(currentPosition()) }.headOption.map {
          case (id, p) => HealTask(mid(), id, p)
        }
        //The agent follows the collective choice
        val leaderHealTask = dangersCollected.toSeq.sortBy {
          case (_, p) => p.distance(currentPosition())
        }.headOption.map {
          case (id, p) => HealTask(mid(), id, p)
        }
        //The agent explore the area near the near
        val exploreTaskArea = Some(
          broadcastPenalized(
            sourceArea,
            influencePenalization,
            ExploreTask(mid(), currentPosition(), grain - influence)
          )
        ).filterNot(_.source == mid()) //safety reason
        //broadcast the collective task choose by leader
        val healTask = G[Option[Task[Program, Actuation]]](sourceArea, leaderHealTask, v => v, nbrRange)
        val tasks = Seq(healTask, localHealTask, exploreTaskArea).collect { case Some(task) => task }
        val selectedTask = planner.eval(mid(), actualLeader, capability, tasks, collective)
        val actuation = selectedTask.call(this) //exec the task
        Actuator.act(node, actuation)
        //Data exported
        node.put("task", selectedTask)
        node.put("sensed", dangersCollected)
        node.put("leader_id", actualLeader)
        if (leader) {
          node.put("dangerDetected", dangersCollected.size)
          node.put("sensed", dangersCollected)
          node.put("influence", grain - (influencePenalization))
          node.put("howMany", healer + explorer)
        }
        mux(mutableArea)(exponentialBackOff(alpha, count = explorer + healer))(0)
      }
    }
    /*Capability sensing
      change the type of node by the behaviour observed during the computation
     */
    node.put("type", localCapabilitySensing())
  }

  override def currentPosition(): Point3D = { //TODO fix alchemist - ScaFi incarnation
    val position = sense[Euclidean2DPosition](LSNS_POSITION)
    Point3D(position.getX, position.getY, 0.0)
  }

  def localCapabilitySensing(): String = {
    val trajectory = recentValues(movementWindow, currentPosition())
    node.put("trajectory", trajectory.last - trajectory.head)
    val distance = trajectory.tail.zip(trajectory.dropRight(1)).map { case (a, b) => a.distance(b) }.sum
    mux(isHealer)("healer")(typeFromDistance(distance)) //local behaviour influence the global structure
  }

  private def isHealer: Boolean = sense[String]("type") == "healer"

  private def isStationary: Boolean = sense[String]("type") == "stationary"

  private def isExplorer: Boolean = sense[String]("type") == "explorer"

  private def isAnimal: Boolean = !node.has("type")

  private def capability: Set[Capability] = Set(Specific(sense[String]("type")))

  private def mutableArea: Boolean = sense[Double]("areaType").toInt == 0

  private def collective: Double = sense[Double]("behaviourType")

  private def exponentialBackOff(alpha: Double, count: Double): Double =
    rep(count)(c => c * (1 - alpha) + alpha * count)

  private def countIn(potential: Double, field: Boolean): Int = C[Double, Int](potential, _ + _, mux(field)(1)(0), 0)

  private def typeFromDistance(distance: Double): String =
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
