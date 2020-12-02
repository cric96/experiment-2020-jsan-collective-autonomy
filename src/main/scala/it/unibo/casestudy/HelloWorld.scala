package it.unibo.casestudy

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._

class HelloWorld extends AggregateProgram with StandardSensors with ScafiAlchemistSupport
  with Gradients {
  override def main(): Any = {

    // Access to node state through "molecule"
    val x = if(node.has("prova")) node.get[Int]("prova") else 1
    // An aggregate operation
    val g = foldhood(0)(_ + _)(nbr(1))
    // Write access to node state
    node.put("g", g)
    println(g)
    // Return value of the program
    g
  }
}
