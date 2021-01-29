package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.nodes.{Animal2D, SimpleNodeManager}
import it.unibo.alchemist.model.interfaces.{Action, Context, Node, Reaction}

case class IllAnimal[T](node : Animal2D[T]) extends AbstractAction[T](node) {
  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = IllAnimal(node) //todo fix this.. the clone must be done in the right place
  var count = 0
  override def execute(): Unit = {
    if(count != 0) {
      new SimpleNodeManager[T](node).put("danger", true)
    }
    count += 1
  }

  override def getContext: Context = Context.LOCAL
}
