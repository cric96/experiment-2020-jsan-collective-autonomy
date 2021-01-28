package it.unibo.casestudy
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import it.unibo.casestudy.WildLifeMonitoring.{MultiTargetPlan, Plan}
import it.unibo.scafi.space.Point3D

import java.util.UUID
class WildLifeMonitoring extends AggregateProgram with StandardSensors with ScafiAlchemistSupport with FieldUtils
  with BlockC with BlockS with BlockT {
  private val grain = 500 //

  def isStation : Boolean = sense("station")
  def isDanger : Boolean = sense("danger")
  def senseAnimalInDanger : Set[ID] = foldhoodPlus(Set.empty[ID])(_ ++ _)(mux(nbr(isDanger)) { Set(nbr(mid)) } { Set.empty })
  override def main(): Any = {
    val leader = mux(isStation) { S(grain, nbrRange) } { false }
    val potential = distanceTo(leader)
    val animalInDanger = senseAnimalInDanger.map(_ -> currentPosition())

    val inMyArea = C[Double, Set[ID]](potential, _ ++ _, Set(mid()), Set.empty)
    if(leader) { println(inMyArea)}
    val animalsFound = C[Double, Set[(ID, P)]](potential, _ ++ _, animalInDanger, Set.empty)
    val animalPossiblePosition = animalsFound.groupBy(_._1)
      .mapValues(data => data.map(_._2))
      .mapValues(data => data.foldLeft(Point3D(0,0,0))(_ + _) / data.size)
    val leaderId = broadcast(leader, mid())

    val plan = G[Plan](leader, MultiTargetPlan(inMyArea, () => true), a => a, nbrRange)
  }
}

object WildLifeMonitoring {
  trait PlanResult
  case class Pending(plan : Plan)
  case class Executed(plan : Plan)
  case object NoPlan

  case class PlanExecutor(id : ID) {
    def exec(plans : Seq[Plan]) : Unit = {

    }
  }

  trait Plan {
    def id : String = UUID.randomUUID().toString
    def isForMe(id : ID) : Boolean
    def perform() : Boolean
  }

  case class MultiTargetPlan(ids : Set[ID], logic : () => Boolean) extends Plan {
    override def isForMe(id: Int): Boolean = ids.contains(id)
    override def perform(): Boolean = logic()
  }
}
