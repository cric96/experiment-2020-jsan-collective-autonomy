package it.unibo

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.{Environment, Position}
import org.danilopianini.util.ListSet

import scala.collection.JavaConverters._
import java.util
import java.util.Optional

package object alchemist {
  //TYPE ENRICHMENT
  implicit class RichPositionAlchemist[P <: Position[P]](p : P) {
    def + (o : P) : P = p.plus(o)
    def - (o : P) : P = p.minus(o)
    def module = Math.sqrt(p.getCartesianCoordinates.map(Math.pow(_, 2)).sum)
  }
  implicit class RichEuclideanPosition2D(p : Euclidean2DPosition) {
    def / (alpha : Double) : Euclidean2DPosition = new Euclidean2DPosition(p.getX / alpha, p.getY / alpha)
    def * (alpha : Double) : Euclidean2DPosition = p / (1.0 / alpha)
  }
  implicit class RichEnv[T, P <: Position[P]](env : Environment[T, P]) {
    val origin : P = env.makePosition((1 to env.getDimensions).map(_ => (0.0 : Number)):_*)
    def makePositionFromSeq(seq : Seq[Double]) : P = env.makePosition(seq.map(x => x : Number):_*)
  }
  //IMPLICIT CONVERSION
  implicit def toList[E](l : ListSet[E]) : List[E] = new util.ArrayList[E](l).asScala.toList
  implicit def optionalToOption[E](p : Optional[E]) : Option[E] = if (p.isPresent) Some(p.get()) else None
}
