package it.unibo.casestudy

import it.unibo.alchemist.model.implementations.nodes.NodeManager
import it.unibo.casestudy.Actuation.{ Explore, Heal, NoActuation }

object Actuator {
  def removeIfPresent(nodeManager: NodeManager, molecule: String): Unit =
    if (nodeManager.has(molecule))
      nodeManager.remove(molecule)
  def act(nodeManager: NodeManager, actuation: Actuation): Unit =
    actuation match {
      case NoActuation =>
        removeIfPresent(nodeManager, "target")
        nodeManager.put("targetId", -1)
        removeIfPresent(nodeManager, "center")
        removeIfPresent(nodeManager, "radius")
      case Heal(id, target) =>
        nodeManager.put("target", target)
        nodeManager.put("targetId", id)
      case Explore(center, radius) =>
        nodeManager.put("center", (center.x, center.y))
        nodeManager.put("radius", radius)
    }
}
