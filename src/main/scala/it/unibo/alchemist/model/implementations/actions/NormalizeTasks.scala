package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist._
import it.unibo.alchemist.model.implementations.nodes.SimpleNodeManager
import it.unibo.alchemist.model.interfaces._
import it.unibo.casestudy.WildlifeTasks.HealTask

/**
 */
case class NormalizeTasks[T, P <: Position[P]](env: Environment[T, P], node: Node[T]) extends AbstractAction[T](node) {

  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = NormalizeTasks(env, node)

  def nodes: Int =
    env.getNodes
      .map(new SimpleNodeManager[T](_))
      .collect { case manager if manager.has("task") => manager.get[Any]("task") }
      .collect { case HealTask(_, target, _) => target }
      .toSet
      .size

  lazy val manager = new SimpleNodeManager[T](node)

  override def execute(): Unit = manager.put("taskReceived", nodes)

  override def getContext: Context = Context.LOCAL

}
