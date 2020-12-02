package it.unibo.alchemist.schemas

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.{Action, Environment, Node, Reaction}
import it.unibo.alchemist.node.DroneNode
import it.unibo.alchemist._
case class Wander[T](env : Environment[T, Euclidean2DPosition], node : DroneNode[T, Euclidean2DPosition], howLong : Double, weight : Double)
  extends MotorSchema[T, Euclidean2DPosition](env, node, weight) {

  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = new Wander[T](env, node, howLong, weight)
  def createRandomDirection = new Euclidean2DPosition(math.random() - 0.5, math.random() - 0.5)
  var randomDirection = createRandomDirection
  var lastTime = 0.0
  def changeDirectionTime : Boolean = env.getSimulation.getTime.toDouble - lastTime > howLong
  override def unweightedVector: Euclidean2DPosition = {
    if(changeDirectionTime) {
      lastTime = env.getSimulation.getTime.toDouble
      randomDirection = createRandomDirection
    }
    randomDirection
  }


}

