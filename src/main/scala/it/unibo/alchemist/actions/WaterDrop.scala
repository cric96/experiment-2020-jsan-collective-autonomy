package it.unibo.alchemist.actions

import it.unibo.alchemist.model.implementations.actions.AbstractAction
import it.unibo.alchemist.model.interfaces.{Action, Context, Environment, Node, Position, Reaction}
import it.unibo.alchemist.node.FireNode

case class WaterDrop[T, P <: Position[P]](node : Node[T], env : Environment[T, P], waterAmount : Double) extends AbstractAction[T](node){
  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = WaterDrop(node, env, waterAmount)
  override def execute(): Unit = {
    FireNode.findFiresInNeighbour(env, node).foreach(node => node.waterDrop(waterAmount, env.getPosition(node)))
  }
  override def getContext: Context = Context.NEIGHBORHOOD
}
