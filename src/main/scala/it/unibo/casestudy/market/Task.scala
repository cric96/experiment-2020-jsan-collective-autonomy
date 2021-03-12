package it.unibo.casestudy.market

case class Effort(capability: String, quantity: Int)

case class Task(efforts: Set[Effort])

case class Bid(id: Int, effort: Effort)
