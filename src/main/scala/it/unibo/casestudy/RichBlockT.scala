package it.unibo.casestudy
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._

import scala.collection.immutable.Queue
trait RichBlockT extends BlockT {
  self : AggregateProgram =>
  def recentValues[T](k: Int, value: T): Queue[T] = {
    rep(Queue[T]()) { case (vls) => (if(vls.size==k) vls.tail else vls) :+ value }
  }
}
