package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist._
import it.unibo.alchemist.model.implementations.nodes.MobileNode
import it.unibo.alchemist.model.interfaces.{ Context, Environment, Position }

/**
 * This action cannot be used outside of a MotorSchemaReaction.
 * This action family is used to describe a motor schema like behaviour. Each action can use some local information to compute an unweighted velocity vector.
 * Combining these actions, some interesting behaviour should be made, as the flocking behaviour of Craig Raylods.
 *
 * @param weight normalizes the velocity computed by the action.
 */
abstract class MotorSchema[T, P <: Position[P]](env: Environment[T, P], mobileNode: MobileNode[T, P], weight: Double)
    extends AbstractAction[T](mobileNode) {

  //TEMPLATE METHOD
  final def velocityVector: P =
    env.makePositionFromSeq(
      unweightedVector.getCartesianCoordinates
        .map(_ * weight)
        .map(_ * mobileNode.maximumSpeed)
    )

  override def execute(): Unit = throw new IllegalArgumentException("use this action inside a MotorSchemaReaction")

  protected def unweightedVector: P //abstract method
  protected def getNeighbourOf(drone: MobileNode[T, P]): Seq[MobileNode[T, P]] =
    env.getNeighborhood(drone).getNeighbors.collect { case node: MobileNode[T, P] => node }.filter {
      case (node) => node.group == mobileNode.group
    }

  override def getContext: Context = Context.GLOBAL

}
