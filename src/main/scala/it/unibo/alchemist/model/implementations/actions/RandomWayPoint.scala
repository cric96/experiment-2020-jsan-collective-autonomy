package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist._
import it.unibo.alchemist.model.implementations.nodes.{MobileNode, SimpleNodeManager}
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.{Action, Environment, Node, Reaction}
import org.apache.commons.math3.random.RandomGenerator
//TODO Unstable, due the concurrency problem, find a solution
case class RandomWayPoint[T](env: Environment[T, Euclidean2DPosition], rand: RandomGenerator,
                      node: MobileNode[T, Euclidean2DPosition],
                      centerX: Double, centerY: Double, radius: Double, thr: Double, maxSleep : Double, weight: Double)
  extends MotorSchema[T, Euclidean2DPosition](env, node, weight) {

  def this(env: Environment[T, Euclidean2DPosition], rand: RandomGenerator,
           node: MobileNode[T, Euclidean2DPosition],
           centerX: Double, centerY: Double, radius: Double, weight: Double) {
    this(env, rand, node, centerX, centerY, radius, RandomWayPoint.DefaultThr, RandomWayPoint.DefaultTime, weight)
  }

  private val diameter = radius * 2
  private lazy val groupLeader : MobileNode[T, Euclidean2DPosition] = env.getNodes.toList.collect {
    case other : MobileNode[T, Euclidean2DPosition] if (node.group == other.group) => other
  }.minBy(_.getId)
  private val velocityMolecule = "velocity"
  private lazy val groupLeaderManger = new SimpleNodeManager[T](groupLeader)
  private var position = randomPositionInCircle
  private var lastTime = 0.0
  private val zeroVelocity = new Euclidean2DPosition(0, 0)
  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = RandomWayPoint[T](env, rand, node, centerX, centerY, radius, thr, maxSleep, weight)
  override def unweightedVector: Euclidean2DPosition = {
    if(node.getId == groupLeader.getId) {
      val leaderUnweightedVector = leaderBehaviour()
      groupLeaderManger.put(velocityMolecule, leaderUnweightedVector)
      leaderUnweightedVector
    } else {
      slaveBehaviour()
    }
  }
  private def leaderBehaviour() : Euclidean2DPosition = {

    if(isSleeping(maxSleep)) {
      zeroVelocity
    } else {
      if(reached) {
        position = randomPositionInCircle
        lastTime = env.getSimulation.getTime.toDouble
      }
      position - env.getPosition(node)
    }
  }
  private def slaveBehaviour() : Euclidean2DPosition = if(groupLeaderManger.has(velocityMolecule)) {
    groupLeaderManger.get[Euclidean2DPosition](velocityMolecule)
  } else {
    zeroVelocity
  }
  private def reached: Boolean = position.getDistanceTo(env.getPosition(node)) < thr
  private def isSleeping(time : Double): Boolean = env.getSimulation.getTime.toDouble - lastTime < time
  private def randomPositionInCircle = new Euclidean2DPosition(randomCoordInCircle(centerX), randomCoordInCircle(centerY))
  private def randomCoordInCircle(centerCoord: Double): Double = {
    val randomPart = rand.nextDouble() * diameter
    centerCoord + (radius - randomPart)
  }
}
object RandomWayPoint {
  private[RandomWayPoint] val DefaultThr = 5
  private[RandomWayPoint] val DefaultTime = 1
}
