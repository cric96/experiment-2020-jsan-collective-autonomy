package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist._
import it.unibo.alchemist.model.implementations.nodes.SimpleNodeManager
import it.unibo.alchemist.model.interfaces._
import it.unibo.casestudy.WildlifeTasks.HealTask

/**
 */
case class AggregateDataAction[T, P <: Position[P]](env: Environment[T, P], node: Node[T])
    extends AbstractAction[T](node) {

  private lazy val manager = new SimpleNodeManager[T](node)

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

  override def execute(): Unit = {
    val meanTaskReceived = computeTaskReceived()
    val meanDistance = computeMeanDistance()
    val healerForArea = computeMeanHealer()
    val emptyZone = computeEmptyZoneCount()
    manager.put("animalTargeted", meanTaskReceived)
    manager.put("meanDistance", meanDistance)
    manager.put("healerForArea", healerForArea)
    manager.put("emptyZoneCount", emptyZone)
  }

  override def getContext: Context = Context.LOCAL

  private def computeTaskReceived(): Int =
    nodesForLeader.map {
      case (key, elements) =>
        key -> elements.collect { case (manager, _) if manager.has("task") => manager.get[Any]("task") }.collect {
          case HealTask(leader_id, target, _) => (leader_id, target)
        }.toSet
    }.map(_._2.size).sum

  private def computeMeanDistance(): Double =
    nodesForLeader.map {
      case (leader, elements) =>
        val leaderNode = env.getNodeByID(leader)
        val distances = elements.map { case (_, node) => env.getDistanceBetweenNodes(leaderNode, node) }.sum
        leader -> distances / elements.size
    }.values.filterNot(_.isNaN).sum / nodesForLeader.size

  private def computeMeanHealer(): Double =
    nodesForLeader.map {
      case (_, elements) =>
        val healerCount = elements
          .filter(_._1.has("type"))
          .count(_._1.get[String]("type") == "healer")
        healerCount
    }.filter(_ < manager.get[Double]("healerNecessary")).sum / nodesForLeader.size

  private def computeEmptyZoneCount(): Int = nodesForLeader.count(_._2.isEmpty)

}
