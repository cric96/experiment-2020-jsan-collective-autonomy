package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.nodes.{MobileNode, NodeManager, SimpleNodeManager}
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.{Action, Environment, Node, Reaction}
import it.unibo.scafi.space.Point3D
import it.unibo.alchemist._
import it.unibo.alchemist.model.implementations.reactions.MotorSchemaReaction
//TODO, find a better way to express the target position, you should use two different behaviour? Or two constructors?
/**
 * MOTOR SCHEMA BEHAVIOUR
 * move toward a position. The position could be expressed as (px, py) or by a molecule value (targetMolecule name).
 * The molecule value should be Option[Point3D].
 * @param px
 * @param py
 * @param targetMolecule
 */
case class SeekSchema[T](env: Environment[T, Euclidean2DPosition], node: MobileNode[T, Euclidean2DPosition],
                         px: Double, py: Double, weight: Double, targetMolecule : String)
  extends MotorSchema[T, Euclidean2DPosition](env, node, weight) {
  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = new SeekSchema[T](env, node, px, py, weight, targetMolecule : String)
  private val manager = new SimpleNodeManager[T](node)
  private val centerOfMass = new Euclidean2DPosition(px, py)
  override def unweightedVector: Euclidean2DPosition = {
    val center = if (manager.has(targetMolecule)) {
      val targetPosition = manager.get[Point3D](targetMolecule)
      new Euclidean2DPosition(targetPosition.x, targetPosition.y)
    } else {
      centerOfMass
    }
    MotorSchemaReaction.normalizedWithSpeed(center - env.getPosition(node), node, env)
  }
}
