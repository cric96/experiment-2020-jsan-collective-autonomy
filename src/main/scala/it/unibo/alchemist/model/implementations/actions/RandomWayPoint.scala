package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.actions.RandomWayPoint.{movementCache, sleepCache}
import it.unibo.alchemist.model.implementations.nodes.MobileNode
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.{Action, Environment, Node, Reaction}
import org.apache.commons.math3.random.RandomGenerator
import it.unibo.alchemist._
//TODO Unstable, due the concurrency problem, find a solution
case class RandomWayPoint[T](env: Environment[T, Euclidean2DPosition], rand: RandomGenerator,
                      node: MobileNode[T, Euclidean2DPosition],
                      centerX: Double, centerY: Double, radius: Double, thr: Double, maxSleep : Double, weight: Double)
  extends MotorSchema[T, Euclidean2DPosition](env, node, weight) {

  private val diameter = radius * 2
  lazy val groupLeader : MobileNode[T, Euclidean2DPosition] = env.getNodes.toList.collect {
    case other : MobileNode[T, Euclidean2DPosition] if (node.group == other.group) => other
  }.minBy(_.getId)

  var position = randomPositionInCircle
  var lastTime = 0.0

  def this(env: Environment[T, Euclidean2DPosition], rand: RandomGenerator,
           node: MobileNode[T, Euclidean2DPosition],
           centerX: Double, centerY: Double, radius: Double, weight: Double) {
    this(env, rand, node, centerX, centerY, radius, 5, 0.01, weight)
  }

  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = RandomWayPoint[T](env, rand, node, centerX, centerY, radius, thr, maxSleep, weight)

  override def unweightedVector: Euclidean2DPosition = {
    if(node.getId == groupLeader.getId) {
      if(reached) {
        position = randomPositionInCircle
      }
      position - env.getPosition(node)
    } else {
      groupLeader.velocity
    }
  }

  private def reached: Boolean = position.getDistanceTo(env.getPosition(node)) < thr

  private def isSleepEnded(time : Double): Boolean = {
    env.getSimulation.getTime.toDouble - lastTime > time
  }
  private def randomPositionInCircle = new Euclidean2DPosition(randomCoordInCircle(centerX), randomCoordInCircle(centerY))

  private def randomCoordInCircle(centerCoord: Double): Double = centerCoord + (radius - rand.nextDouble() * diameter)
}

object RandomWayPoint {
  private val movementCache : collection.mutable.Map[String, Euclidean2DPosition] = collection.mutable.HashMap()
  private val sleepCache : collection.mutable.Map[String, String] = collection.mutable.HashMap()
}