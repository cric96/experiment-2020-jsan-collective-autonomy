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

  type AuditionK = String

  type OfferPicked = (K, Bid)

  type BidPicked = (Boolean, Set[Bid])

  def period: Int = 10

  def capabilities: Map[String, Int] = Map("bibo" -> 10, "pippo1" -> 20)

  case class Continuation[D](data: D, continue: Boolean)

  type Offers = (Boolean, Option[(K, AuditionK, Bid)])

  val metric: Metric = nbrRange //
  override def main(): Any =
    rep(State.empty) { state =>
      var vState = state
      vState = vState.addTasks(newTask())
      val keys = mux(vState.tasks.isEmpty)(Set.empty[K])(Set(mid()))
      val audition =
        if (vState.audition.isEmpty && vState.tasks.nonEmpty)
          Some(Audition(vState.tasks.head, Set.empty, Set.empty))
        else
          vState.audition

      vState = vState.changeAudition(audition)

      val auditions = csspawn(auditionSpawn, keys, vState.audition)

      val pickOne = mux(vState.bidDone.isEmpty) {
        auditions.find { case (leaderK, (auditKey, efforts)) => efforts.nonEmpty }.map {
          case (leaderK, (auditKey, efforts)) => (leaderK, auditKey, Bid(mid(), efforts.head))
        }
      }(vState.bidDone)
      vState = vState.updateBid(pickOne)

      val offers = csspawn[K, Offers, Set[Bid]](offersSpawn, keys, (vState.audition.nonEmpty, pickOne))
      val currentAudition = vState.audition
      val evalOffers = branch(impulsesEvery(period) && offers.contains(mid()) && currentAudition.isDefined) {
        val offersForMe = offers(mid())
        val founded = currentAudition.get.todo
          .filter(data => offersForMe.exists(_.effort.capability == data.capability))
          .map(data => offersForMe.find(_.effort.capability == data.capability))
          .map(_.get)
        currentAudition.map(audit => audit.copy(founded = founded ++ audit.founded))
      }(currentAudition)
      vState = vState.changeAudition(evalOffers)
      val bidsFounded: Set[Bid] = audition.map(_.founded).getOrElse(Set.empty[Bid])

      val winAudition = csspawn[K, BidPicked, Set[Bid]](winBidSpawn, keys, (vState.audition.nonEmpty, bidsFounded))
      val haveWin = pickOne.flatMap(bid => winAudition.find { case (_, bids) => bids.contains(bid._3) })
      vState = mux(haveWin.nonEmpty)(vState.updateBid(None))(vState)

      val finished = vState.audition
        .filter(audit => audit.founded.size == audit.selected.efforts.size)
        .map(_.selected)

      vState = vState.changeAudition(mux(finished.isEmpty)(vState.audition)(None))
      if (finished.nonEmpty) {
        println(finished, vState.tasks.headOption)
        println(vState.removeTask().tasks)
        println(vState.tasks)
      }
      node.put("offer", offers)
      node.put("audition", currentAudition)
      node.put("tasks", vState.tasks)
      node.put("auditions", auditions)
      node.put("auditions", auditions)
      node.put("audition", vState.audition)
      branch(vState.tasks.nonEmpty && vState.tasks.headOption == finished)(vState.removeTask())(vState)
    }

  val auditionSpawn: K => Option[Audition] => ((AuditionK, Set[Effort]), Spawn.Status) = leader =>
    audition => {
      val source = leader == mid()
      val status = evalStatus(leader, audition.nonEmpty)
      val key = audition.map(_.key).getOrElse("_")
      val data = branch(audition.nonEmpty)(audition.head.todo)(Set.empty)
      val efforts = G[Set[Effort]](source, data, a => a, metric)
      val auditionKey = broadcast(source, key)
      (auditionKey -> efforts, status)
    }

  val offersSpawn: K => Offers => (Set[Bid], Spawn.Status) = leader => {
    case (continue, bid) =>
      val source = leader == mid()
      val status = evalStatus(leader, continue)
      val potential = distanceTo(source, metric)
      val bids = Set(bid)
        .filter(_.nonEmpty)
        .map(_.get)
        .filter(_._1 == leader)
        .map(_._3)
      val bidsForLeader = C[Double, Set[Bid]](potential, _ ++ _, bids, Set.empty)
      (bidsForLeader, status)
  }

  val winBidSpawn: K => BidPicked => (Set[Bid], Spawn.Status) = leader => {
    case (continue, bids) =>
      val source = leader == mid()
      val status = evalStatus(leader, continue)
      val winner = G[Set[Bid]](source, bids, a => a, metric)
      winner -> status
  }

  def evalStatus(leader: K, enabled: Boolean): Spawn.Status = {
    val areaEnabled = broadcast(leader == mid(), enabled)
    if (areaEnabled)
      Spawn.Output
    else
      Spawn.Terminated
  }

  def newTask(): Queue[Task] =
    if (randomGen.nextDouble() < 0.001) Queue(Task(Set(Effort("pippo", mid()), Effort("pippo2", mid() + 1))))
    else Queue.empty

}
