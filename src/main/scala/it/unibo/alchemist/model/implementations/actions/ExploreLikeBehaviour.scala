package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.nodes.MobileNode
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import org.apache.commons.math3.random.RandomGenerator

trait ExploreLikeBehaviour[T] {
  def env: Environment[T, Euclidean2DPosition]
  def rand: RandomGenerator
  def node: MobileNode[T, Euclidean2DPosition]
  def thr : Double
  def centerX : Double
  def centerY  : Double
  def radius : Double
  lazy val diameter : Double = radius * 2
  protected def reached(targetPosition : Euclidean2DPosition): Boolean = targetPosition.getDistanceTo(env.getPosition(node)) < thr
  protected def randomPositionInCircle() = new Euclidean2DPosition(randomCoordinateInCircle(centerX), randomCoordinateInCircle(centerY))
  protected def randomCoordinateInCircle(centerCoord: Double): Double = centerCoord + (radius - rand.nextDouble() * diameter)
}
