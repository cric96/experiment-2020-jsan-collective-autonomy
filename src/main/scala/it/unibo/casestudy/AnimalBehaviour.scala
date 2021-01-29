package it.unibo.casestudy

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.ScafiAlchemistSupport
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._

class AnimalBehaviour extends AggregateProgram with StandardSensors with ScafiAlchemistSupport with FieldUtils {
  def isDanger : Boolean = sense("danger")

  override def main(): Any = {
    align[String, Set[ID]]("danger"){ k => foldhoodPlus(Set.empty[ID])(_ ++ _)(mux(nbr(isDanger)) { Set(nbr(mid)) } { Set.empty })}
  }
}
