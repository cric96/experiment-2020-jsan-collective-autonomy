package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist._
import it.unibo.alchemist.model.implementations.nodes.{ MobileNode, SimpleNodeManager }
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.{ Action, Environment, Node, Reaction }
import org.apache.commons.math3.random.RandomGenerator

/**
 * MOTOR SCHEMA BEHAVIOUR
 * follow the logic of Random Waypoint Model: the random waypoint model is a random model for the movement of mobile users,
 * and how their location, velocity and acceleration change over time (from https://en.wikipedia.org/wiki/Random_waypoint_model).
 * This behaviour is attached to the entire group, namely collectively the nodes behaves as random way point.
 * The key idea here is that the group used the same velocity of the leader (i.e. the node with the lowest id in the group).
 * As Explore Area, the nodes move randomly inside a circle.
 * @param centerX @see ExploreArea
 * @param centerY @see ExploreArea
 * @param radius @see ExploreArea
 * @param thr @see ExploreArea
 * @param maxSleep the delta time in which the group stand still
 */
case class RandomWayPoint[T](
  env: Environment[T, Euclidean2DPosition],
  rand: RandomGenerator,
  node: MobileNode[T, Euclidean2DPosition],
  centerX: Double,
  centerY: Double,
  radius: Double,
  thr: Double,
  maxSleep: Double,
  weight: Double
) extends MotorSchema[T, Euclidean2DPosition](env, node, weight)
    with ExploreLikeBehaviour[T] {
  def this(
    env: Environment[T, Euclidean2DPosition],
    rand: RandomGenerator,
    node: MobileNode[T, Euclidean2DPosition],
    centerX: Double,
    centerY: Double,
    radius: Double,
    weight: Double
  ) {
    this(env, rand, node, centerX, centerY, radius, RandomWayPoint.DefaultThr, RandomWayPoint.DefaultTime, weight)
  }
  private lazy val groupLeader: MobileNode[T, Euclidean2DPosition] = env.getNodes.toList.collect {
    case other: MobileNode[T, Euclidean2DPosition] if (node.group == other.group) =>
      other //take the node in the same group
  }.minBy(_.getId)
  private val velocityMolecule = "velocity"
  private lazy val groupLeaderManger = new SimpleNodeManager[T](groupLeader)
  private var position = randomPositionInCircle
  private var lastTime = 0.0
  private val zeroVelocity = new Euclidean2DPosition(0, 0)
  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] =
    RandomWayPoint[T](env, rand, node, centerX, centerY, radius, thr, maxSleep, weight)
  override def unweightedVector: Euclidean2DPosition =
    if (node.getId == groupLeader.getId) {
      val leaderUnweightedVector = leaderBehaviour()
      groupLeaderManger.put(velocityMolecule, leaderUnweightedVector)
      leaderUnweightedVector
    } else
      slaveBehaviour()
  private def leaderBehaviour(): Euclidean2DPosition =
    if (isSleeping(maxSleep))
      zeroVelocity
    else {
      if (reached(position)) {
        position = randomPositionInCircle()
        lastTime = env.getSimulation.getTime.toDouble
      }
      position - env.getPosition(node)
    }
  private def slaveBehaviour(): Euclidean2DPosition =
    if (groupLeaderManger.has(velocityMolecule))
      groupLeaderManger.get[Euclidean2DPosition](velocityMolecule)
    else
      zeroVelocity
  private def isSleeping(time: Double): Boolean = env.getSimulation.getTime.toDouble - lastTime < time
}
object RandomWayPoint {
  private[RandomWayPoint] val DefaultThr = 5
  private[RandomWayPoint] val DefaultTime = 1
}
