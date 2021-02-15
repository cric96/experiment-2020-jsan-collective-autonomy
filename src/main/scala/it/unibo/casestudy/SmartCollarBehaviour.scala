package it.unibo.casestudy

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.{ScafiAlchemistSupport, _}
import it.unibo.scafi.space.Point3D

class SmartCollarBehaviour extends AggregateProgram with StandardSensors with ScafiAlchemistSupport with FieldUtils {
  def isDanger : Boolean = sense("danger")
  override def main(): Any = {
    SmartCollarBehaviour.dangerAnimalField(this)
    val save = SmartCollarBehaviour.anyHealer(this)
    val danger = mux(save) { false } { node.get[Boolean]("danger")}
    node.put("danger", danger)
  }
  override def currentPosition(): Point3D = { //TODO fix alchemist - ScaFi incarnation
    val position = sense[Euclidean2DPosition](LSNS_POSITION)
    Point3D(position.getX, position.getY, 0.0)
  }
}

object SmartCollarBehaviour {
  def dangerAnimalField(interpreter: AggregateProgram with StandardSensors with FieldUtils with ScafiAlchemistSupport) : Map[ID, P] = {
    import interpreter._
    val isStation : Boolean = node.get("station")
    val isDanger : Boolean = sense("danger")
    align[String, Map[ID, P]]("danger"){
      //TODO it is right to do in this way?
      (k : String) => branch(isStation) { Map.empty[ID, P] } {
        foldhood(Map.empty[ID, P]){ (left, right) => combineDangerMap(left, right) }{
          mux(nbr(isDanger)) { Map(nbr(mid()) -> nbr(currentPosition()))} { Map.empty }
        }
      }
    }
  }

  def anyHealer(interpreter: AggregateProgram with StandardSensors with FieldUtils with ScafiAlchemistSupport) : Boolean = {
    import interpreter._
    import excludingSelf._
    lazy val isHealer : Boolean = if(node.has("type")) {
      sense[String]("type") == "healer"
    } else {
      false
    }
    align[String, Boolean]("heal"){
      //TODO it is right to do in this way?
      (k : String) => anyHood(nbr(isHealer))
    }
  }

  def combineDangerMap(left : Map[ID, P], right : Map[ID, P]) : Map[ID, P] = {
    val same = left.keySet intersect right.keySet
    val union = left ++ right
    val mergeSameNode = same.map(id => id -> (left(id) + right(id)) / 2)
    union ++ mergeSameNode
  }
}
