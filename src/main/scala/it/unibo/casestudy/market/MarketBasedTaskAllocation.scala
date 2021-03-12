package it.unibo.casestudy.market

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import it.unibo.casestudy._

import scala.collection.immutable.Queue

class MarketBasedTaskAllocation
    extends AggregateProgram
    with CustomSpawn
    with StandardSensors
    with BlockG
    with BlockC
    with BlockT
    with ScafiAlchemistSupport
    with StateManagement {

  type K = Int

  def period: Int = 5

  case class Continuation[D](data: D, continue: Boolean)

  case class Audition(selected: Task, founded: Set[Bid], done: Set[Effort]) {

    def todo: Set[Effort] = selected.efforts.filterNot(effort => founded.exists(_.effort == effort))

  }

  type Offers = (Boolean, Option[(K, Bid)])

  val metric: Metric = nbrRange //
  override def main(): Any = {
    val localTasks = rep(Queue.empty[Task]) { task =>
      val tasks = task ++ newTask
      val (audition, finished) = rep((Option.empty[Audition], Option.empty[Task])) {
        case (audition, finished) =>
          val keys = mux(tasks.isEmpty)(Set.empty[K])(Set(mid()))
          val currentAudition: Option[Audition] =
            if (audition.isEmpty && tasks.nonEmpty)
              Some(Audition(tasks.head, Set.empty, Set.empty))
            else
              audition
          val efforts = cspawn(auditionSpawn, keys, currentAudition)
          val pickOne = efforts
            .find(_._2.nonEmpty)
            .map(data => data._1 -> Bid(mid(), data._2.head))
          val offers = cspawn[K, Offers, Set[Bid]](offersSpawn, keys, (currentAudition.nonEmpty, pickOne))
          node.put("offer", offers)
          node.put("audition", currentAudition)
          node.put("picked", pickOne)
          node.put("tasks", tasks)
          node.put("efforts", efforts)
          val auditionUpdated = branch(offers.contains(mid()) && currentAudition.isDefined) {
            val offersForMe = offers(mid())
            currentAudition.get.todo
            val founded = currentAudition.get.todo
              .filter(data => offersForMe.exists(_.effort.capability == data.capability))
              .map(data => offersForMe.find(_.effort.capability == data.capability))
              .map(_.get)
            currentAudition.map(audit => audit.copy(founded = founded ++ audit.founded))
          }(audition)
          val finished = auditionUpdated
            .filter(audit => audit.founded.size == audit.selected.efforts.size)
            .map(_.selected)
          (mux(finished.isEmpty)(auditionUpdated)(None), finished)
      }
      branch(tasks.nonEmpty && tasks.headOption == finished)(tasks.tail)(tasks)
    }
  }

  val auditionSpawn: K => Option[Audition] => SpawnReturn[Set[Effort]] = leader =>
    audition => {
      val source = leader == mid()
      val hasTask = broadcast(source, audition.nonEmpty)
      branch(hasTask) {
        val data = branch(source)(audition.head.todo)(Set.empty)
        val efforts = G[Set[Effort]](source, data, a => a, metric)
        SpawnReturn(efforts, status = true)
      } {
        SpawnReturn(Set.empty, status = false)
      }
    }

  val offersSpawn: K => Offers => SpawnReturn[Set[Bid]] = leader => {
    case (continue, bid) =>
      val source = leader == mid()
      val potential = distanceTo(source, metric)
      val bids = Set(bid)
        .filter(_.nonEmpty)
        .map(_.get)
        .filter(_._1 == leader)
        .map(_._2)
      val bidsForLeader = C[Double, Set[Bid]](potential, _ ++ _, bids, Set.empty)
      val hasTask = broadcast(source, continue)
      branch(source) {
        SpawnReturn(bidsForLeader, continue)
      } {
        SpawnReturn(bidsForLeader, hasTask)
      }
  }

  def newTask(): Queue[Task] =
    if (randomGen.nextDouble() < 0.001) Queue(Task(Set(Effort("pippo", mid()), Effort("pippo2", mid() + 1))))
    else Queue.empty

}
