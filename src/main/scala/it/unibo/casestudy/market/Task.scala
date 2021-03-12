package it.unibo.casestudy.market

import it.unibo.casestudy.market.State.AuditionK

import java.util.UUID

case class Effort(capability: String, quantity: Int)

case class Task(efforts: Set[Effort])

case class Bid(id: Int, effort: Effort)

case class Audition(
  selected: Task,
  founded: Set[Bid],
  done: Set[Effort],
  key: AuditionK = UUID.randomUUID().toString
) {

  def todo: Set[Effort] = selected.efforts.filterNot(effort => founded.exists(_.effort == effort))

}
