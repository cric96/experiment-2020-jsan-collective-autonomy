package it.unibo.alchemist.node
import it.unibo.alchemist.model.implementations.nodes.{ScafiNode, SimpleNodeManager}
import it.unibo.alchemist.model.implementations.utils.BidimensionalGaussian
import it.unibo.alchemist.model.interfaces.{Environment, Node, Position}
import it.unibo.alchemist._
class FireNode[T, P <: Position[P]](env : Environment[T, P], var initialAmplitude : Double, var initialRange : Double) extends ScafiNode[T, P](env) {
  private lazy val firePosition = env.getPosition(this)
  private lazy val manager = new SimpleNodeManager[T](this)
  def range : Double = initialRange
  def amplitude : Double = initialAmplitude
  def gaussian : BidimensionalGaussian = new BidimensionalGaussian(amplitude, firePosition.getCoordinate(0), firePosition.getCoordinate(1), range, range)
  def intensity(p : P) : Double = gaussian.value(p.getCoordinate(0), p.getCoordinate(1))
  def expand() : Unit = { //todo find a better way..
    initialAmplitude += 2
    initialRange += 1
    manager.put("range", initialRange)
    manager.put("amplitude", amplitude)
  }
  def waterDrop(amount : Double, position:  P) : Unit = {
    if(position.getDistanceTo(firePosition) < range) {
      this.initialAmplitude -= amount
      if(initialAmplitude < 0) {
        this.env.removeNode(this)
      }
    }
  }
}

object FireNode {
  def findFiresInNeighbour[T, P <: Position[P]](env : Environment[T, P], node : Node[T]) : Seq[FireNode[T, P]] = {
    env.getNeighborhood(node).getNeighbors.collect { case fire : FireNode[T, P] => fire }
      .filter(fire => env.getDistanceBetweenNodes(node, fire) < fire.range)
  }
}