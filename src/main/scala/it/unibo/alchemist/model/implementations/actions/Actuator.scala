package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.nodes.MobileNode
import it.unibo.alchemist.model.interfaces._

/**
 * Gives a speed to the mobile node associated. It is a trivial implementation of AbstractMoveNode in which, the next position is
 * dictated by a velocity vector.
 * @param velocity the initial velocity vector, it could changes over the time
 */
class Actuator[T, P <: Position[P]](env: Environment[T, P], mobileNode: MobileNode[T, P], var velocity: P)
    extends AbstractMoveNode[T, P](env, mobileNode) {

  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = new Actuator(env, mobileNode, velocity)

  override def getNextPosition: P = velocity

}
