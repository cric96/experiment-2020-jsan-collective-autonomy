package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.nodes.{Animal2D, SimpleNodeManager}
import it.unibo.alchemist.model.interfaces.{Action, Context, Environment, Node, Position, Reaction}
import it.unibo.alchemist.scala.PimpMyAlchemist.time2Double

case class IllAnimal[T, P <: Position[P]](env : Environment[T, P], node : Animal2D[T], timeLimit : Double) extends AbstractAction[T](node) {
  def this(env : Environment[T, P], node : Animal2D[T]) {
    this(env, node, Double.PositiveInfinity)
  }
  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = IllAnimal(env, node, timeLimit) //todo fix this.. the clone must be done in the right place
  private val manger = new SimpleNodeManager[T](node)
  private var firstFire : Boolean = true

  override def execute(): Unit = if (firstFire) {
    firstFire = false
  } else if (env.getSimulation.getTime < timeLimit) {
    manger.put("danger", true)
  }

  override def getContext: Context = Context.LOCAL
}
