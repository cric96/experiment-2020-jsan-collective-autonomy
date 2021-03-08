package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist._
import it.unibo.alchemist.model.implementations.nodes.SimpleNodeManager
import it.unibo.alchemist.model.interfaces._
import it.unibo.casestudy.WildlifeTasks.HealTask

/**
 */
case class AggregateDataAction[T, P <: Position[P]](env: Environment[T, P], node: Node[T])
    extends AbstractAction[T](node) {

  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = AggregateDataAction(env, node)

  def nodesForLeader: Map[Int, List[(SimpleNodeManager[T], Node[T])]] =
    env.getNodes
      .map(node => (new SimpleNodeManager[T](node), node))
      .filter(_._1.has("leader_id"))
      .groupBy(_._1.get[Int]("leader_id"))
      .map { case (leader, slaves) => leader -> slaves.filter(_._2.getId != leader) }

  def nodes: Int =
    env.getNodes
      .map(new SimpleNodeManager[T](_))
      .collect { case manager if manager.has("task") => manager.get[Any]("task") }
      .collect { case HealTask(leader_id, target, _) => (leader_id, target) }
      .groupBy(a => a)
      .keySet
      .size

  lazy val manager = new SimpleNodeManager[T](node)

  override def execute(): Unit = {
    val meanTaskReceived = nodesForLeader.map {
      case (key, elements) =>
        key -> elements.collect { case (manager, _) if manager.has("task") => manager.get[Any]("task") }.collect {
          case HealTask(leader_id, target, _) => (leader_id, target)
        }.toSet
    }.map(_._2.size).sum
    val meanDistance = nodesForLeader.map {
      case (leader, elements) =>
        val leaderNode = env.getNodeByID(leader)
        val distances = elements.map { case (_, node) => env.getDistanceBetweenNodes(leaderNode, node) }.sum
        leader -> distances / elements.size
    }.values.sum / nodesForLeader.size
    manager.put("taskReceived", meanTaskReceived)
    manager.put("meanDistance", meanDistance)
  }

  override def getContext: Context = Context.LOCAL

}
