package it.unibo.casestudy

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.{ ScafiAlchemistSupport, _ }

/**
 * some utils for manage the "smart collar" behaviours
 */
trait SmartCollarUtils
    extends AggregateProgram
    with StandardSensors
    with ScafiAlchemistSupport
    with FieldUtils
    with BlockT {
  self: AggregateProgram
    with StandardSensors
    with ScafiAlchemistSupport
    with FieldUtils
    with BlockT
    with RichStateManagement =>

  def isDanger: Boolean = sense("danger")

  /**
   * @return the animal in danger found locally
   */
  protected def dangerAnimalField(): Map[ID, P] = {
    val isStation: Boolean = sense[Boolean]("station")
    branch(isStation)(Map.empty[ID, P]) {
      foldhood(Map.empty[ID, P])((left, right) => combineDangerMap(left, right)) {
        mux(nbr(isDanger)) {
          Map(nbr(mid()) -> nbr(currentPosition()))
        } {
          Map.empty
        }
      }
    }
  }

  /**
   * @return true if an animal could be healed, false otherwise
   */
  protected def canHealAnimal(): Boolean = {
    val targetIdField: Map[ID, Int] = excludingSelf.reifyField(nbr(sense[Int]("targetId")))
    targetIdField.values.count(_ == mid()) >= sense[Double]("healerNecessary")
  }

  /**
   * combination logic of danger animal map
   */
  protected def combineDangerMap(left: Map[ID, P], right: Map[ID, P]): Map[ID, P] = {
    val same = left.keySet intersect right.keySet
    val union = left ++ right
    val mergeSameNode = same.map(id => id -> (left(id) + right(id)) / 2)
    union ++ mergeSameNode
  }

}
