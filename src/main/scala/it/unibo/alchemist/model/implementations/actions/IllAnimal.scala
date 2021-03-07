package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.nodes.{ Animal2D, SimpleNodeManager }
import it.unibo.alchemist.model.interfaces.{ Action, Context, Environment, Node, Position, Reaction }
import it.unibo.alchemist.toList
import org.apache.commons.math3.random.RandomGenerator
import scala.collection.mutable.ArrayBuffer

/**
 * MOTOR SCHEMA BEHAVIOUR
 * a "ill" animal generator. When executed, peek a random animal and change its status to danger.
 * NB! this action should be attach only to one node in the system.
 * @param timeLimit describe the temporal window in which the action produce danger animal. When timeLimit passed, no animal change its status.
 */
case class IllAnimal[T, P <: Position[P]](
  env: Environment[T, P],
  rand: RandomGenerator,
  node: Node[T],
  timeLimit: Double
) extends AbstractAction[T](node) {
  def this(env: Environment[T, P], node: Node[T], rand: RandomGenerator) {
    this(env, rand, node, Double.PositiveInfinity)
  }
  private lazy val animals: List[Animal2D[T]] = env.getNodes.collect { case n: Animal2D[T] => n }
  private lazy val manager = new SimpleNodeManager[T](node)
  private var fireCount = 0
  override def cloneAction(n: Node[T], r: Reaction[T]): Action[T] = IllAnimal(env, rand, node, timeLimit)
  override def execute(): Unit = {
    if (env.getSimulation.getTime.toDouble < timeLimit) {
      val shuffledAnimals = shuffle(animals).map(new SimpleNodeManager[T](_)) //avoid to compute random number each time
      shuffledAnimals
        .filter(_.has("danger"))
        .filterNot(_.get[Boolean]("danger"))
        .headOption
        .foreach { animal =>
          fireCount += 1
          animal.put("danger", true)
        }
    }
    manager.put("dangerNodeSpawn", fireCount)
  }
  override def getContext: Context = Context.LOCAL
  //take from
  private def shuffle(animals: List[Animal2D[T]]): List[Animal2D[T]] = {
    val buf = new ArrayBuffer[Animal2D[T]] ++= animals
    def swap(i1: Int, i2: Int) {
      val tmp = buf(i1)
      buf(i1) = buf(i2)
      buf(i2) = tmp
    }
    for (n <- buf.length to 2 by -1) {
      val k = rand.nextInt(n)
      swap(n - 1, k)
    }
    (buf).result().toList
  }
}
