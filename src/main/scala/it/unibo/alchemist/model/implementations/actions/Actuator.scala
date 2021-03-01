package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.nodes.MobileNode
import it.unibo.alchemist.model.interfaces._

class Actuator[T, P <: Position[P]](env : Environment[T, P], droneNode: MobileNode[T, P], var unweightedVector : P) extends AbstractMoveNode[T, P](env, droneNode) {
  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = new Actuator(env, droneNode, unweightedVector)

  override def getNextPosition: P = unweightedVector
}
