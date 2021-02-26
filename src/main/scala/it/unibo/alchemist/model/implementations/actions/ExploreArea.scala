package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.nodes.MobileNode
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.{Action, Environment, Node, Reaction}
import org.apache.commons.math3.random.RandomGenerator

case class ExploreArea[T](env: Environment[T, Euclidean2DPosition], rand: RandomGenerator,
                          node: MobileNode[T, Euclidean2DPosition],
                          centerX: Double, centerY: Double, radius: Double, thr: Double, weight: Double)
  extends MotorSchema[T, Euclidean2DPosition](env, node, weight) {
  private val diameter = radius * 2
  private var targetPosition = randomPositionInCircle()
  private var behaviour : SeekPosition[T] = SeekPosition(env, node, targetPosition.getX, targetPosition.getY, weight)

  def this(env: Environment[T, Euclidean2DPosition], rand: RandomGenerator,
           node: MobileNode[T, Euclidean2DPosition],
           centerX: Double, centerY: Double, radius: Double, weight: Double) {
    this(env, rand, node, centerX, centerY, radius, 0.01, weight)
  }

  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = ExploreArea[T](env, rand, node, centerX, centerY, radius, thr, weight)

  override def unweightedVector: Euclidean2DPosition = {
    if (reached()) {
      targetPosition = randomPositionInCircle()
      behaviour = SeekPosition(env, node, targetPosition.getX, targetPosition.getY, weight)
    }
    behaviour.unweightedVector
  }

  private def reached(): Boolean = targetPosition.getDistanceTo(env.getPosition(node)) < thr

  private def randomPositionInCircle() = new Euclidean2DPosition(randomCoordInCircle(centerX), randomCoordInCircle(centerY))

  private def randomCoordInCircle(centerCoord: Double): Double = centerCoord + (radius - rand.nextDouble() * diameter)
}
