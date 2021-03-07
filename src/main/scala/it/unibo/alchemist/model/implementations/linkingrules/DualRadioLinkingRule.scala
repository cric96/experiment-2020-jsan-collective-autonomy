package it.unibo.alchemist.model.implementations.linkingrules

import it.unibo.alchemist.model.implementations.neighborhoods.Neighborhoods
import it.unibo.alchemist.model.implementations.nodes.LoraStation
import it.unibo.alchemist.model.interfaces._

import scala.collection.JavaConverters.{ asScalaBufferConverter, bufferAsJavaListConverter }

/**
 * Simulates a network in which nodes have two time of connection, LoRa and Bluetooth low energy.
 * The LoRa nodes work as station.
 * @param lora the range of lora station
 * @param ble the range of bluetooth node
 */
private class DualRadioLinkingRule[T, P <: Position[P]](lora: Double, ble: Double) extends LinkingRule[T, P] {

  override def computeNeighborhood(center: Node[T], env: Environment[T, P]): Neighborhood[T] =
    center match {
      case node: LoraStation[T, P] => Neighborhoods.make(env, center, env.getNodesWithinRange(node, lora))
      case node =>
        val stations = env.getNodesWithinRange(node, lora).asScala.collect { case node: LoraStation[T, P] => node }
        Neighborhoods.make(env, center, (stations ++ env.getNodesWithinRange(node, ble).asScala).asJava)
    }

  override def isLocallyConsistent: Boolean = true

}
