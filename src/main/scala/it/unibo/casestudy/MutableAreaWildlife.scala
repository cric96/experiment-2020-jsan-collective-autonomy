package it.unibo.casestudy
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
class MutableAreaWildlife extends AggregateProgram with Gradients
  with StandardSensors with FieldUtils with BlockT with BlockC
  with ScafiAlchemistSupport {
  val nodeElementsInArea = 2
  val deltaArea = 10
  val initialArea : Double = 50
  val changeTime = 2
  def isHealer : Boolean = sense("type") == "healer"
  def isStationary : Boolean = sense("type") == "stationary"
  def isExploratory : Boolean = sense("type") == "exploratory"
  def limitedGradient(field: Boolean, max: Double): Double = {
    val gradient = classicGradient(field, nbrRange)
    branch(gradient < max) { gradient } { Double.PositiveInfinity }
  }

  def broadcastIn[A](g: Double, field: A): A = {
    rep(field) { case (value) =>
      mux(g == 0.0 || g.isInfinity) {
        field
      } {
        excludingSelf.minHoodSelector[Double, A](nbr { g} + nbrRange())(nbr(value)).getOrElse(field)
      }
    }
  }

  def countIn(potential: Double, field: Boolean): Int = {
    node.put("local", branch(field && !potential.isInfinity) { 1 } { 0 })
    C[Double, Int](potential, _ + _, branch(field && !potential.isInfinity) { 1 } { 0 }, 0)
  }

  override def main(): Any = {
    val (area) = rep(initialArea) {
      area => {
        val influence = broadcastIn[Double](classicGradient(isStationary, nbrRange), area) //influence field: associate for each point a influence area
        val potentialField = limitedGradient(isStationary, influence) //the influence area field: where stationary node has influence
        node.put("potential", potentialField)
        val countHealer = countIn(potentialField, isHealer) //number of healer in the influence area
        node.put("healer", countHealer)
        val countExploratory = countIn(potentialField, isExploratory)  //number of exploratory in the influence area
        val updatedInfluenced = branch(isStationary && impulsesEvery(changeTime)) { //the influence field could be expanded or reduced according to the node type in the zone
          node.put("healer", countHealer)
          node.put("exploratory", countExploratory)
          if (countHealer < nodeElementsInArea && countExploratory < nodeElementsInArea) {
            influence * 1.1
          } else {
            influence / 1.1
          }
        } {
          influence
        }
        (updatedInfluenced)
      }
    }
    val source = limitedGradient(isStationary, area)
    val inArea = !source.isInfinity
    if(isStationary) { node.put("area", area)}
    val value: Int = broadcastIn[ID](source, mid())
    node.put("leader_id", value)
    //val light = mux(inArea) { ledAll to "red" } { ledAll to "blue" }
    (area)
  }
}
