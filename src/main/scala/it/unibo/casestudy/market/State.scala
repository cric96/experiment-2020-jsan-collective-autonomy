package it.unibo.casestudy.market

import it.unibo.casestudy.market.State.{ AuditionK, K }

import scala.collection.immutable.Queue

case class State(
  tasks: Queue[Task] = Queue.empty,
  audition: Option[Audition] = None,
  effortsRequired: Option[(AuditionK, Set[Effort])] = None,
  bidDone: Option[(K, AuditionK, Bid)] = None,
  capabilities: Map[String, Int] = Map.empty,
  inExecution: Option[(AuditionK, Bid)] = None
) {

  def addTasks(task: Queue[Task]): State = this.copy(tasks = tasks ++ task)

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
      case None         => capabilities
    }
    this.copy(capabilities = newCapabilities)
  }

  def updateExecution(execution: Option[(AuditionK, Bid)]): State = this.copy(inExecution = execution)

  def updateBid(bid: Option[(K, AuditionK, Bid)]): State = this.copy(bidDone = bid)

}

object State {

  type AuditionK = String

  type K = Int

  def empty: State = State()

}
