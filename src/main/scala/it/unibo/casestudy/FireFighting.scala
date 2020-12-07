package it.unibo.casestudy
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import it.unibo.scafi.space.Point3D
class FireFighting extends AggregateProgram with StandardSensors with ScafiAlchemistSupport
  with BlockG with BlockS with BlockC with BlockT with CustomSpawn with TimeUtils with FieldUtils
  with StateManagement with ProcessDSL {
  private val leaderArea = 50.0
  private val maxTemperature = 26.0
  private val droneCountToAlert = 1
  private val origin : P = Point3D(0.0, 0.0, 0.0)
  private val NullTemperature : TemperatureData[P] = TemperatureData[P](origin, 0.0, 0)
  override def main(): Any = {
    //leader election
    val leader = mux(node.has("station")) { S(leaderArea, nbrRange) } { false }
    //potential field
    val distanceToLeader = distanceTo(leader)
    //data gathering
    val highestTemperature = C[Double, TemperatureData[P]](
      potential = distanceToLeader,
      acc = aggregationStrategy,
      local = TemperatureData(position, temperature),
      Null = NullTemperature
    )
    //do some operation on temperature..
    val normalized = highestTemperature.normalize
    //broadcast the goal to the zone
    val goalPosition = broadcast(leader, mux(isDanger(normalized))(normalized.dronePosition)(position))
    val danger = broadcast(leader, isDanger(normalized))
    val leaderId = broadcast(leader, mid())
    //put data into alchemist simulation
    node.put("center", goalPosition)
    node.put("leader_id", leaderId)
  }
  def temperature : Double = sense[Double]("temperature")
  def position : P = {
    val alchemist = sense[Euclidean2DPosition](LSNS_POSITION)
    Point3D(alchemist.getX, alchemist.getY, 0.0)
  }
  def isDanger(data : TemperatureData[P]) : Boolean = data.howMany > droneCountToAlert
  def aggregationStrategy(acc : TemperatureData[P], value : TemperatureData[P]) : TemperatureData[P] = {
    (acc, value) match {
      case (maximum, nodePerception) if nodePerception.temperature > maxTemperature && maximum.temperature < nodePerception.temperature =>
        nodePerception.combine(maximum)
      case (maximum, node) if node.temperature > maxTemperature =>
        maximum.combine(node)
      case (acc, _) => acc
    }
  }
}
