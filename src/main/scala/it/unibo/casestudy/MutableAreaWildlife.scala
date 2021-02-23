package it.unibo.casestudy
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._

//A test without using block S and leveraging on process + align.
class MutableAreaWildlife extends AggregateProgram with Gradients
  with StandardSensors with FieldUtils with BlockT with BlockC
  with BlockG with ScafiAlchemistSupport with ProcessDSL with StateManagement
  with CustomSpawn with TimeUtils {
  val nodeElementsInArea = 5
  val deltaArea = 1.1
  val maxArea = 700
  val initialArea : Double = maxArea
  val changeTime = 4
  def isHealer : Boolean = sense[String]("type") == "healer"
  def isStationary : Boolean = sense[String]("type") == "stationary"
  def isExploratory : Boolean = sense[String]("type") == "exploratory"
  def limitedGradient(field: Boolean, max: Double): Double = {
    val gradient = classicGradient(field, nbrRange)
    branch(gradient < max) { gradient } { Double.PositiveInfinity }
  }

  def broadcastIn[A](g: Double, field: A): A = {
    rep(field) { case (value) =>
      mux(g == 0.0 || g.isInfinity) {
        field
      } {
        excludingSelf.minHoodSelector[Double, A](nbr { g } + nbrRange())(nbr(value)).getOrElse(field)
      }
    }
  }

  def countIn(potential: Double, field: Boolean): Int = {
    node.put("local", branch(field && !potential.isInfinity) { 1 } { 0 })
    C[Double, Int](potential, _ + _, branch(field && !potential.isInfinity) { 1 } { 0 }, 0)
  }
  //NOT WORK
  def influenceEval : Double = rep(initialArea) {
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
          influence * deltaArea
        } else {
          influence / deltaArea
        }
      } {
        influence
      }
      (updatedInfluenced)
    }
  }

  override def main(): Any = {
    val area = rep(initialArea)(area => {
      val broadcastArea = broadcastIn(classicGradient(isStationary, nbrRange), area)
      val influenceArea = limitedGradient(isStationary, broadcastArea)
      val leader = broadcastIn(influenceArea, mid())
      val healerCount = alignedExecution[ID, Int](_ => {
        countIn(influenceArea, isHealer)
      })(leader)
      val exploratoryCount = alignedExecution[ID, Int](_ => {
        countIn(influenceArea, isExploratory)
      })(leader)
      branch(impulsesEvery(changeTime)) {
        val currentArea = if (exploratoryCount < nodeElementsInArea && healerCount < nodeElementsInArea) {
          area * deltaArea
        } else {
          area / deltaArea
        }
        if(currentArea > maxArea) { maxArea } else { currentArea }
      } {
        area
      }
    })
    val broadcastArea = broadcastIn(classicGradient(isStationary, nbrRange), area)
    val influenceArea = limitedGradient(isStationary, broadcastArea)
    val inArea = !influenceArea.isInfinity
    if(isStationary) { node.put("influence", area)}
    val leader: Int = broadcastIn[ID](influenceArea, mid())

    node.put("areaInfluence", broadcastArea)
    node.put("areaInfluence", influenceArea)
    node.put("leader_id", leader)
  }
}
