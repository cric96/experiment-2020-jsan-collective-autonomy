package it.unibo.alchemist.actions

import it.unibo.alchemist.model.implementations.actions.AbstractAction
import it.unibo.alchemist.model.interfaces.{Action, Context, Node, Position, Reaction}
import it.unibo.alchemist.node.FireNode

case class ExpandFire[T, P <: Position[P]](fireNode : FireNode[T, P]) extends AbstractAction[T](fireNode){
  private val area = 50.0 //todo, think if you want to put outside.
  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = ExpandFire(fireNode)
  override def execute(): Unit = fireNode.area += 1
  override def getContext: Context = Context.LOCAL
}
