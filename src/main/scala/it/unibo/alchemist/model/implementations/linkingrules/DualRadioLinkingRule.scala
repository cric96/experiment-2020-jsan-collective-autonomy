package it.unibo.alchemist.model.implementations.linkingrules

import it.unibo.alchemist.model.implementations.neighborhoods.Neighborhoods
import it.unibo.alchemist.model.implementations.nodes.LoraStation
import it.unibo.alchemist.model.interfaces.{Environment, LinkingRule, Neighborhood, Node, Position}

import scala.collection.JavaConverters.{asScalaBufferConverter, bufferAsJavaListConverter}
//TODO improve..
private class DualRadioLinkingRule[T, P <: Position[P]](lora : Double, ble : Double) extends LinkingRule[T, P] {
  override def computeNeighborhood(center: Node[T], env: Environment[T, P]): Neighborhood[T] = center match {
    case node : LoraStation[T, P] => Neighborhoods.make(env, center, env.getNodesWithinRange(node, lora))
    case node => val bleNeigh = Neighborhoods.make(env, center, env.getNodesWithinRange(node, ble))
      val stations = env.getNodesWithinRange(node, lora).asScala.collect { case node : LoraStation[T, P] => node }
      Neighborhoods.make(env, center, (stations ++ env.getNodesWithinRange(node, ble).asScala).asJava)

  }

  override def isLocallyConsistent: Boolean = false
}
