package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.nodes.MobileNode
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.{Action, Environment, Node, Reaction}
import org.apache.commons.math3.random.RandomGenerator

/**
 * MOTOR SCHEMA BEHAVIOUR
 * move randomly in the space. Internally it computes a velocity vector and use it for howLong time.
 *
 * @param howLong the time in which the velocity remains the same.
 */
case class Wander[T](
  env: Environment[T, Euclidean2DPosition],
  rand: RandomGenerator,
  node: MobileNode[T, Euclidean2DPosition],
  howLong: Double,
  weight: Double
) extends MotorSchema[T, Euclidean2DPosition](env, node, weight) {

  private var randomDirection = createRandomDirection

  private var lastTime = 0.0

  def this(
    env: Environment[T, Euclidean2DPosition],
    rand: RandomGenerator,
    node: MobileNode[T, Euclidean2DPosition],
    howLong: Double
  ) {
    this(env, rand, node, howLong, 1.0)
  }

  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = new Wander[T](env, rand, node, howLong, weight)

  override def unweightedVector: Euclidean2DPosition = {
    if (changeDirectionTime) {
      lastTime = env.getSimulation.getTime.toDouble
      randomDirection = createRandomDirection
    }
    randomDirection
  }

  private def createRandomDirection = new Euclidean2DPosition(rand.nextDouble() - 0.5, rand.nextDouble() - 0.5)

  private def changeDirectionTime: Boolean = env.getSimulation.getTime.toDouble - lastTime > howLong

}
