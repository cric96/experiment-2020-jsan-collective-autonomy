package it.unibo.alchemist.actions

import it.unibo.alchemist.model.implementations.actions.AbstractAction
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.{Action, Context, Environment, Node, Reaction}
import it.unibo.alchemist.node.FireNode2D
import org.apache.commons.math3.random.RandomGenerator

case class FireSpawn[T](rand : RandomGenerator, node : Node[T], environment: Environment[T, Euclidean2DPosition],
                        minX : Double, minY : Double, maxX : Double, maxY : Double) extends AbstractAction[T](node) {
  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = FireSpawn(this.rand, node, environment, minX, minY, maxX, maxY)
  override def execute(): Unit = environment.addNode(new FireNode2D[T](environment), randomPositionInBound)
  override def getContext: Context = Context.GLOBAL
  private def randomPositionInBound : Euclidean2DPosition = {
    def randomCoordinate(lowerBound : Double, upperBound : Double) : Double = lowerBound + (rand.nextDouble() * (upperBound - lowerBound))
    new Euclidean2DPosition(randomCoordinate(minX, maxX), randomCoordinate(minY, maxY))
  }
}
