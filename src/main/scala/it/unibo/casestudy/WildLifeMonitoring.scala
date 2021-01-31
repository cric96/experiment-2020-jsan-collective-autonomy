package it.unibo.casestudy
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.implementations.nodes.{NodeManager, SimpleNodeManager}
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import it.unibo.casestudy.WildLifeMonitoring.{MultiTargetPlan, Plan, ProgramType}
import it.unibo.scafi.space.Point3D

import java.util.UUID
import scala.collection.JavaConverters.iterableAsScalaIterableConverter
class WildLifeMonitoring extends AggregateProgram with StandardSensors with ScafiAlchemistSupport with FieldUtils
  with BlockC with BlockS with BlockT {
  def movingAround(p : P) : Unit = node.put("target",Some(p))
  def noTargetPosition() : Unit = node.put("target",None)
  private val grain = 500 //area for each station
  /** shortcut for sense("station) */
  def isStation : Boolean = sense("station")
  /**
   * shortcut for sense("danger"). In this case, we suppose that the animal-installed device could "sense" if the animal
   * is in danger or not. In general, this computed by the observing-animal node (e.g. via camera) or by the area-leader
   */
  def isDanger : Boolean = sense("danger")
  /**
   * "instantly" heal the near in danger animal (specified by the id). For future experiments, this operation should take some
   * time to be performed.
   * @return always true
   */
  def heal(animalTag : ID, interpreter : ProgramType) : Boolean = {
    val nodeAlchemist = interpreter.alchemistEnvironment.getNodeByID(mid())
    val found = interpreter.alchemistEnvironment.getNeighborhood(nodeAlchemist).asScala
      .filter(_.getId == animalTag)
    found.foreach(a => a.setConcentration(new SimpleMolecule("danger"), false))
    true
  }
  /**
   * the behaviour encapsulate inside the rescue plan. It use the aggregate interpreter to perform the logic
   * locally. The node go toward the position and try to heal the node.
   */
  def reachAndHeal(id : ID, p : P, interpreter: ProgramType): Boolean = {
    interpreter.node.put("target", Some(p))
    val nodeAlchemist = interpreter.alchemistEnvironment.getNodeByID(mid())
    val found = interpreter.alchemistEnvironment.getNeighborhood(nodeAlchemist).asScala.exists(_.getId == id) //find better way
    //val found = anyHood(nbr(align[String, ID]("mid")((key : String) => mid())) == id) found a better way
    interpreter.branch(found) { interpreter.heal(id, interpreter) } { false }
  }
  /**
   * moving nodes search animal in danger. This function return an id set of animal in danger. In this example, the nodes
   * check only the "danger" molecule.
   */
  def senseAnimalInDanger : Set[ID] = align[String, Set[ID]]("danger"){ k => foldhoodPlus(Set.empty[ID])(_ ++ _)(mux(nbr(isDanger)) { Set(nbr(mid)) } { Set.empty })}
  override def main(): Any = {
    val leader = mux(isStation) { S(grain, nbrRange) } { false } //leader election on stations.
    val potential = distanceTo(leader) //potential field from a device to a leader.
    val animalsInDanger = senseAnimalInDanger.map(_ -> currentPosition()) //get all animal in danger sensed by the node
    val inMyArea = C[Double, Set[ID]](potential, _ ++ _, mux(leader) { Set.empty[ID] } { Set(mid()) }, Set.empty) //all ids in the "leader-area"
    val animalsInArea = C[Double, Set[(ID, P)]](potential, _ ++ _, animalsInDanger, Set.empty) //all animal in danger found in this area
    val animalPossiblePosition = animalsInArea.groupBy(_._1) //the leader try to found the right position of a certain animal
      .mapValues(data => data.map(_._2))
      .mapValues(data => data.foldLeft(Point3D(0,0,0))(_ + _) / data.size)
    val toExecute = animalPossiblePosition //create plan for the nodes (currently, all node in the area tries to heal the animal)
      .map { case (id, p) => id -> MultiTargetPlan(inMyArea, (interpreter) => reachAndHeal(id, p, interpreter))}
      .toSeq
      .sortBy(_._1)
      .map(_._2)
      .headOption //get only the first plan to fulfill
    val plan = G[Option[Plan]](leader, toExecute, a => a, nbrRange) //broadcast the plan to slaves
    val leaderPosition = G[P](leader, currentPosition(), a => a, nbrRange) //tell the leader position to the slaves
    //STRUCTURAL PART, the node stand near the leader or continue to move using their logic (flock + wander in an area)
    branch(node.has("goToLeader")) { noTargetPosition() } { movingAround(leaderPosition) }
    //EXEC THE PLAN ONLY IF THE NODE ISN'T A STATION
    branch(isStation) {} {
      plan.filter(_.isForMe(mid())).foreach(_.perform(this))
    }
    val leaderId = broadcast(leader, mid()) //for output purpose
    node.put("leader_id", leaderId)
  }
  override def currentPosition(): Point3D = { //TODO fix alchemist - ScaFi incarnation
    val position = sense[Euclidean2DPosition](LSNS_POSITION)
    Point3D(position.getX, position.getY, 0.0)
  }
}

object WildLifeMonitoring {
  //utility data for plan spreading. Think if it is better to use processes. TODO create better structure...
  type ProgramType = WildLifeMonitoring
  trait PlanResult
  case class Pending(plan : Plan)
  case class Executed(plan : Plan)
  case object NoPlan

  trait Plan {
    def id : String = UUID.randomUUID().toString
    def isForMe(id : ID) : Boolean
    def perform(interpreter : ProgramType) : Boolean
  }

  case class MultiTargetPlan(ids : Set[ID], logic : (ProgramType) => Boolean) extends Plan {
    override def isForMe(id: Int): Boolean = ids.contains(id)
    override def perform(interpreter : ProgramType): Boolean = logic(interpreter)
  }
}
