package it.unibo.casestudy

object Utils {
  def max[P : Ordering](p : P*) : P = p.max
  def min[P : Ordering](p : P*) : P = p.min
}
