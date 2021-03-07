package it.unibo.casestudy
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._

trait PenalizedG {
  self: AggregateProgram with StandardSensors with FieldUtils =>
  def penalizedGradient(source: Boolean, penalization: Double): Double =
    rep(Double.PositiveInfinity) { d =>
      mux(source)(penalization)(minHoodPlus(nbr(d) + nbrRange()))
    }
  def penalizedG[D](source: Boolean, penalization: Double)(field: D)(acc: D => D): D = {
    val g = penalizedGradient(source, penalization)
    rep(field) { value =>
      val neighbourValue = excludingSelf.reifyField(acc(nbr(value))) ++ Map(mid() -> field)
      val distances = excludingSelf.reifyField(nbr(g) + nbrRange()) ++ Map(mid() -> Double.PositiveInfinity)
      //Map(mid() -> field) is the fallback value. When there aren't neighbours, the result is field.
      mux(source)(field)(neighbourValue(distances.minBy(_._2)._1))
    }
  }
  def broadcastPenalized[D](source: Boolean, penalization: Double, data: D): D =
    penalizedG(source, penalization)(data)(d => d)
}
