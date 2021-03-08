package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.nodes.{MobileNode, SimpleNodeManager}
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.{Action, Environment, Node, Reaction}
import org.apache.commons.math3.random.RandomGenerator

/**
 * MOTOR SCHEMA BEHAVIOUR
 * Describe a behaviour that, in time, explore a circular area. Internally, compute a point inside the area and produce a
 * vector toward it. When the node reach the target (i.e. the distance is smaller then a threshold passed), a new position is
 * computed.
 *
 * @param centerX
 * @param centerY
 * @param radius
 * @param thr the threshold to consider a position reached.
 * @tparam T
 */
case class ExploreArea[T](
  env: Environment[T, Euclidean2DPosition],
  rand: RandomGenerator,
  node: MobileNode[T, Euclidean2DPosition],
  centerXCoord: Double,
  centerYCoord: Double,
  radiusCircle: Double,
  thr: Double,
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
    this(env, rand, node, centerX, centerY, radius, thr = 0.01, weight)
  }

  private lazy val manager = new SimpleNodeManager[T](node)

  private var targetPosition: Euclidean2DPosition = randomPositionInCircle()

  private var behaviour: SeekSchema[T] =
    SeekSchema(env, node, targetPosition.getX, targetPosition.getY, weight, "target")

  override def centerX: Double = if (manager.has("center")) manager.get[(Double, Double)]("center")._1 else centerXCoord

  override def centerY: Double = if (manager.has("center")) manager.get[(Double, Double)]("center")._2 else centerYCoord

  override def radius: Double = if (manager.has("radius")) manager.get[Double]("radius") else radiusCircle

  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] =
    ExploreArea[T](env, rand, node, centerX, centerY, radius, thr, weight)

  override def unweightedVector: Euclidean2DPosition = {
    if (reached(targetPosition)) {
      targetPosition = randomPositionInCircle()
      manager.put("targetPositionComputed", targetPosition)
      behaviour = SeekSchema(env, node, targetPosition.getX, targetPosition.getY, weight, "target")
    }
    behaviour.unweightedVector
  }

}
