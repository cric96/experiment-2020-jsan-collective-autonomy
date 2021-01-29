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
  override def currentPosition(): Point3D = {
    val position = sense[Euclidean2DPosition](LSNS_POSITION)
    Point3D(position.getX, position.getY, 0.0)
  }
  private val grain = 500 //
  import excludingSelf._
  def isStation : Boolean = sense("station")
  def isDanger : Boolean = sense("danger")

  def heal(id : ID, interpreter : ProgramType) : Boolean = {
    val node = interpreter.alchemistEnvironment.getNodeByID(id.toInt)

    val nodeAlchemist = interpreter.alchemistEnvironment.getNodeByID(mid())
    val found = interpreter.alchemistEnvironment.getNeighborhood(nodeAlchemist).asScala
      .filter(a => a.getConcentration(new SimpleMolecule("danger")).asInstanceOf[Boolean])
    found.foreach(a => a.setConcentration(new SimpleMolecule("danger"), false))
    true
  }

  def reachAndHeal(id : ID, p : P, interpreter: ProgramType): Boolean = {
    interpreter.node.put("target", Some(p))
    val nodeAlchemist = interpreter.alchemistEnvironment.getNodeByID(mid())
    val found = interpreter.alchemistEnvironment.getNeighborhood(nodeAlchemist).asScala.exists(_.getId == id)
    //val found = anyHood(nbr(align[String, ID]("mid")((key : String) => mid())) == id) found a better way
    interpreter.branch(found) { interpreter.heal(id, interpreter) } { false }
  }

  def senseAnimalInDanger : Set[ID] = align[String, Set[ID]]("danger"){ k => foldhoodPlus(Set.empty[ID])(_ ++ _)(mux(nbr(isDanger)) { Set(nbr(mid)) } { Set.empty })}

  override def main(): Any = {
    val leader = mux(isStation) { S(grain, nbrRange) } { false }
    val potential = distanceTo(leader)
    val animalInDanger = senseAnimalInDanger.map(_ -> currentPosition())
    val inMyArea = C[Double, Set[ID]](potential, _ ++ _, mux(leader) { Set.empty[ID] } { Set(mid()) }, Set.empty)

    val animalsFound = C[Double, Set[(ID, P)]](potential, _ ++ _, animalInDanger, Set.empty)

    val animalPossiblePosition = animalsFound.groupBy(_._1)
      .mapValues(data => data.map(_._2))
      .mapValues(data => data.foldLeft(Point3D(0,0,0))(_ + _) / data.size)

    val leaderId = broadcast(leader, mid())

    val toExecute = animalPossiblePosition.map { case (id, p) => MultiTargetPlan(inMyArea, (interpreter) => reachAndHeal(id, p, interpreter))}.headOption
    val plan = G[Option[Plan]](leader, toExecute, a => a, nbrRange)
    val leaderPosition = G[P](leader, currentPosition(), a => a, nbrRange)
    node.put("leader_id", leaderId)
    node.put("target", None /* Some(leaderId) */)
    node.put("hasPlan", plan.isDefined)
    branch(isStation) {} {
      plan.filter(_.isForMe(mid())).foreach(_.perform(this))
    }
  }
}

object WildLifeMonitoring {
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
