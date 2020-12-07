package it.unibo

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.{P, positionOrdering}
import it.unibo.scafi.space.Point3D

import java.util.Optional

package object casestudy {
  type Bounded[P] = ScafiIncarnationForAlchemist.Builtins.Bounded[P]
  //TYPE CLASSES
  implicit val pointBound = new Bounded[P] {
    override def bottom: Point3D = Point3D(-500, -500, -500)
    override def top: Point3D = Point3D(500, 500, 500)
    override def compare(a: Point3D, b: Point3D): Int = positionOrdering.compare(a, b)
  }
  implicit val booleanBound = new Bounded[Boolean] {
    override def top: Boolean = true
    override def bottom: Boolean = false
    override def compare(a: Boolean, b: Boolean): Int = Ordering.Boolean.compare(a, b)
  }
  //IMPLICIT CONVERSION
  implicit def OptionalToOption[E](p : Optional[E]) : Option[E] = if (p.isPresent) Some(p.get()) else None
}
