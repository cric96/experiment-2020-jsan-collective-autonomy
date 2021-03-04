package it.unibo.casestudy

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.{ScafiAlchemistSupport, _}
//TODO
trait SmartCollarBehaviour extends AggregateProgram with StandardSensors with ScafiAlchemistSupport with FieldUtils with BlockT {
  self :  AggregateProgram with StandardSensors with ScafiAlchemistSupport with FieldUtils with BlockT =>
  def isDanger : Boolean = sense("danger")

  def dangerAnimalField() : Map[ID, P] = {
    val isStation : Boolean = node.get("station")
    val isDanger : Boolean = sense("danger")
    branch(isStation) { Map.empty[ID, P] } {
      foldhood(Map.empty[ID, P]) { (left, right) => combineDangerMap(left, right) } {
        mux(nbr(isDanger)) {
          Map(nbr(mid()) -> nbr(currentPosition()))
        } {
          Map.empty
        }
      }
    }
  }

  def canHealAnimal() : Boolean = {
    val targetIdField : Map[ID, Int] = excludingSelf.reifyField(nbr(sense[Int]("targetId")))
    targetIdField.values.toSet.contains(mid())
  }

  def combineDangerMap(left : Map[ID, P], right : Map[ID, P]) : Map[ID, P] = {
    val same = left.keySet intersect right.keySet
    val union = left ++ right
    val mergeSameNode = same.map(id => id -> (left(id) + right(id)) / 2)
    union ++ mergeSameNode
  }
}