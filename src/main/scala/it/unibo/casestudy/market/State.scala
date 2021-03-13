package it.unibo.casestudy.market

import it.unibo.casestudy.market.State.{ AuditionK, K }

import scala.collection.immutable.Queue

case class State(
  tasks: Queue[Task] = Queue.empty,
  audition: Option[Audition] = None,
  effortsRequired: Option[(AuditionK, Set[Effort])] = None,
  bidDone: Option[(K, AuditionK, Bid)] = None,
  capabilities: Map[String, Int] = Map.empty,
  inExecution: List[(K, AuditionK, Bid, Effort)] = List.empty
) {

  def addTasks(task: Queue[Task]): State = this.copy(tasks = tasks ++ task)

  def canCompute(effort: Effort): Boolean =
    capabilities.get(effort.capability).exists(_ > effort.quantity)

  def alreadyEval(leader: K, auditionK: AuditionK, capability: String): Boolean =
    inExecution.exists {
      case (`leader`, `auditionK`, _, Effort(`capability`, _)) => true
      case _                                                   => false
    }

  def removeTask(): State = this.copy(tasks = tasks.tail)

  def clearTask(): State = this.copy(tasks = Queue.empty)

  def changeAudition(audition: Option[Audition]): State = this.copy(audition = audition)

  def allocCapability(capability: String, amount: Int): State = {
    val newCapabilities = capabilities.get(capability).map(_ - amount) match {
      case Some(amount) => capabilities + (capability -> amount)
      case None         => capabilities
    }
    this.copy(capabilities = newCapabilities)
  }

  def deallocCapability(capability: String, amount: Int): State = {
    val newCapabilities = capabilities.get(capability).map(_ + amount) match {
      case Some(amount) => capabilities + (capability -> amount)
      case None         => capabilities + (capability -> amount)
    }
    this.copy(capabilities = newCapabilities)
  }

  def updateExecution(execution: Option[(K, AuditionK, Bid, Effort)]): State =
    execution match {
      case Some(executionData) => this.copy(inExecution = executionData :: inExecution)
      case None                => this
    }

  def cleanExecution[E](map: Map[K, (AuditionK, E)]): State = {
    val toRemove = inExecution.filter {
      case (k, auditionK: AuditionK, _, Effort(_, q)) =>
        q <= 0 && map.contains(k) && auditionK != map(k)._1 || q < -100
    }

    val reallocCapability = toRemove.map { case (k, _, bid, _) => (bid.effort.capability, bid.effort.quantity) }
      .groupBy(_._1)
      .mapValues(_.map(_._2).sum)

    val capabilitiesUpdate = capabilities.map {
      case (k, qnt) => k -> (reallocCapability.getOrElse(k, 0) + qnt)
    }
    val cleared = inExecution.filterNot(a => toRemove.contains(a))
    this.copy(inExecution = cleared, capabilities = capabilitiesUpdate)
  }

  def exec(): State = {
    val exec = this.inExecution.map {
      case (leader, k, bid, effort) => (leader, k, bid, effort.copy(quantity = effort.quantity - 1))
    }
    this.copy(inExecution = exec)
  }

  def completed(): Map[K, Set[Bid]] =
    inExecution.filter(_._4.quantity <= 0).groupBy(_._1).mapValues { elements =>
      elements.map { case (_, k, bid, _) => (bid) } toSet
    }

  def updateBid(bid: Option[(K, AuditionK, Bid)]): State =
    (bidDone, bid) match {
      case (None, Some(bidC)) =>
        val effort = bidC._3.effort
        this.allocCapability(effort.capability, effort.quantity).copy(bidDone = bid)
      case _ => this.copy(bidDone = bid)
    }

}

object State {

  type AuditionK = String

  type K = Int

  def empty: State = State()

}
