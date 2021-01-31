package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.nodes.{Animal2D, SimpleNodeManager}
import it.unibo.alchemist.model.interfaces.{Action, Context, Node, Reaction}

case class IllAnimal[T](node : Animal2D[T]) extends AbstractAction[T](node) {
  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = IllAnimal(node) //todo fix this.. the clone must be done in the right place
  private val manger = new SimpleNodeManager[T](node)
  private var firstFire : Boolean = true

  override def execute(): Unit = if (firstFire) {
    firstFire = false
  } else {
    manger.put("danger", true)
  }

  override def getContext: Context = Context.LOCAL
}
