package it.unibo.alchemist.actions

import it.unibo.alchemist.model.implementations.actions.AbstractAction
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.reactions.Event
import it.unibo.alchemist.model.implementations.timedistributions.ExponentialTime
import it.unibo.alchemist.model.interfaces._
import it.unibo.alchemist.node.FireNode
import org.apache.commons.math3.random.RandomGenerator
import scala.collection.JavaConverters.seqAsJavaListConverter
case class FireSpawn[T](rand : RandomGenerator, node : Node[T], environment: Environment[T, Euclidean2DPosition],
                        minX : Double, minY : Double, maxX : Double, maxY : Double, expandRate : Double) extends AbstractAction[T](node) {
  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = FireSpawn(this.rand, node, environment, minX, minY, maxX, maxY, expandRate)
  override def execute(): Unit = {
    val node = new FireNode[T, Euclidean2DPosition](environment)
    val fireExpanded : Action[T] = new ExpandFire(node)
    val reaction = new Event[T](node, new ExponentialTime[T](expandRate, environment.getSimulation.getTime, rand))
    reaction.setActions(List(fireExpanded).asJava)
    node.addReaction(reaction)
    environment.addNode(node, randomPositionInBound)

  }
  override def getContext: Context = Context.GLOBAL
  private def randomPositionInBound : Euclidean2DPosition = {
    def randomCoordinate(lowerBound : Double, upperBound : Double) : Double = lowerBound + (rand.nextDouble() * (upperBound - lowerBound))
    new Euclidean2DPosition(randomCoordinate(minX, maxX), randomCoordinate(minY, maxY))
  }
}
