package it.unibo.casestudy

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._

/**
 * A penalized (i.e the source node returns an arbitrary value instead of 0.0) version of block G.
 * This version could be used to give a penalty of some source over others
 */
trait PenalizedG {
  self: AggregateProgram with StandardSensors with FieldUtils =>

  /**
   * create a penalized gradient field where the source have `penalized` value
   * e.g. (suppose a manhattan metric)
   * T true source field
   * F false source field
   * p penalization value
   * F,Inf - F,Inf - F,Inf
   *   |       |       |
   * F,Inf - T,Inf - F,Inf
   *   |       |       |
   * F,Inf - F,Inf - F,Inf
   *
   *         |
   *      produces
   *         |
   *         V
   * F,p+2 - F,p+1 - F,p+2
   *   |       |       |
   * F,p+1 -  T,p  - F,p+1
   *   |       |       |
   * F,p+2 - F,p+1 - F,p+2
   *
   * @param source the source field
   * @param penalization the penalization of the source
   */
  def penalizedGradient(source: Boolean, penalization: Double): Double =
    rep(Double.PositiveInfinity) { d =>
      mux(source)(penalization)(minHoodPlus(nbr(d) + nbrRange()))
    }

  /**
   * The penalized version of block G.
   * @param source the source field
   * @param penalization the penalization of the source
   * @param field the field that gonna be evaluated according to the penalized gradient
   * @param acc how the field changes over the gradient
   * @tparam D field type
   */
  def penalizedG[D](source: Boolean, penalization: Double)(field: D)(acc: D => D): D = {
    val g = penalizedGradient(source, penalization)
    rep(field) { value =>
      val neighbourValue = excludingSelf.reifyField(acc(nbr(value))) ++ Map(mid() -> field)
      val distances = excludingSelf.reifyField(nbr(g) + nbrRange()) ++ Map(mid() -> Double.PositiveInfinity)
      //Map(mid() -> field) is the fallback value. When there aren't neighbours, the result is field.
      mux(source)(field)(neighbourValue(distances.minBy(_._2)._1))
    }
  }

  /**
   * The penalized version of broadcast
   * @param source the source field
   * @param penalization the penalization of the source
   * @param data the data that gonna be broadcast according the gradient
   */
  def broadcastPenalized[D](source: Boolean, penalization: Double, data: D): D =
    penalizedG(source, penalization)(data)(d => d)

}
