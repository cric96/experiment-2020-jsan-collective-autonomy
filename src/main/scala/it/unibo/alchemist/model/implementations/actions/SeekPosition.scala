package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.nodes.{MobileNode, SimpleNodeManager}
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.{Action, Environment, Node, Reaction}
import it.unibo.scafi.space.Point3D
import it.unibo.alchemist._
import it.unibo.alchemist.model.implementations.reactions.MotorSchemaReaction
//TODO add the target molecule argument
case class SeekPosition[T](env: Environment[T, Euclidean2DPosition], node: MobileNode[T, Euclidean2DPosition], px: Double, py: Double, weight: Double)
  extends MotorSchema[T, Euclidean2DPosition](env, node, weight) {
  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = new SeekPosition[T](env, node, px, py, weight: Double)

  val manager = new SimpleNodeManager[T](node)
  val centerOfMass = new Euclidean2DPosition(px, py)

  override def unweightedVector: Euclidean2DPosition = {
    val center = if (manager.has("target")) {
      val scafi = manager.get[Option[Point3D]]("target")
      scafi match {
        case None => centerOfMass
        case Some(pos) => new Euclidean2DPosition(pos.x, pos.y)
      }
    } else {
      centerOfMass
    }

    MotorSchemaReaction.normalizedWithSpeed(center - env.getPosition(node), node, env)
  }
}
