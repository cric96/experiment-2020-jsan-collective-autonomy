package it.unibo.alchemist.actions

import it.unibo.alchemist.model.implementations.actions.AbstractAction
import it.unibo.alchemist.model.interfaces._

import scala.util.{Success, Try}
import it.unibo.alchemist._
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.implementations.nodes.SimpleNodeManager
import it.unibo.alchemist.node.FireNode
case class FireSensing[T, P <: Position[P]](node : Node[T], env : Environment[T, P], baseTemperatureMolecule : String) extends AbstractAction[T](node) {
  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = FireSensing(node, env, baseTemperatureMolecule)
  private val manager = new SimpleNodeManager[T](node)
  override def execute(): Unit = {
    val fire = env.getNeighborhood(node).getNeighbors.collect { case (fire: FireNode[T, P]) => fire }
    val baseTemperature : Double = findInLayers[java.math.BigDecimal](baseTemperatureMolecule).get.doubleValue()
    val fireIntensity : Double = if (fire.isEmpty) { 0.0 } else { fire.maxBy(_.intensity(env.getPosition(node))).intensity(env.getPosition(node)) }
    manager.put("temperature", baseTemperature + fireIntensity)
  }
  override def getContext: Context = Context.LOCAL
  private def findInLayers[A](name: String): Option[A] = {
    val layer: Option[Layer[T, P]] = env.getLayer(new SimpleMolecule(name))
    layer.map(l => l.getValue(env.getPosition(node)))
      .map(value => Try(value.asInstanceOf[A]))
      .collect { case Success(value) => value }
  }
}