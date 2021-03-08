package it.unibo.casestudy

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._

import scala.collection.immutable.Queue

/**
 * Include some operation to manage the state in an aggregate program
 */
trait RichStateManagement {
  self: AggregateProgram =>

  /**
   * compute and store a field composed by the last values computed by a local node
   * @param k the temporal window
   * @param value value expression stored
   * @tparam T the data type
   * @return a Queue that contains the last k values
   */
  def recentValues[T](k: Int, value: T): Queue[T] =
    rep(Queue[T]()) { case (vls) => (if (vls.size == k) vls.tail else vls) :+ value }

}
