package it.unibo.casestudy
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.{Layer, Position}
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import it.unibo.scafi.space.Point3D

import java.util.Optional
import scala.util.{Success, Try}
class FireFighting extends AggregateProgram with StandardSensors with ScafiAlchemistSupport
  with BlockG with BlockS with BlockC with BlockT with CustomSpawn with TimeUtils with FieldUtils
  with StateManagement with ProcessDSL {

  implicit def OptionalToOption[E](p : Optional[E]) : Option[E] = if (p.isPresent) Some(p.get()) else None

  private def findInLayers[A](name : String) : Option[A] = {
    val layer : Option[Layer[Any, Position[_]]] = alchemistEnvironment.getLayer(new SimpleMolecule(name))
    val node = alchemistEnvironment.getNodeByID(mid())
    layer.map(l => l.getValue(alchemistEnvironment.getPosition(node)))
      .map(value => Try(value.asInstanceOf[A]))
      .collect { case Success(value) => value }
  }

  //TODO fix in the alchemist
  def senseEnv[A](name: String): A = {
    findInLayers[A](name).get
  }

  val grain = 50.0

  val maxTemperature = 26.0

  val droneCountToAlert = 1

  implicit val pointBound = new ScafiIncarnationForAlchemist.Builtins.Bounded[P] {
    override def bottom: Point3D = Point3D(-500, -500, -500)

    override def top: Point3D = Point3D(500, 500, 500)

    override def compare(a: Point3D, b: Point3D): Int = positionOrdering.compare(a, b)
  }

  def temperature : Double = senseEnv[Double]("constant") + senseEnv[Double]("fire")

  def position : P = {
    val alchemist = sense[Euclidean2DPosition](LSNS_POSITION)
    Point3D(alchemist.getX, alchemist.getY, 0.0)
  }

  override def main(): Any = {
    def aggregationStrategy(acc : (P, Double, Int), value : (P, Double, Int)) : (P, Double, Int) = {
      (acc, value) match {
        case ((_, maxSensedTemp, howMay), (nodePosition, temp, me)) if (temp > maxTemperature && maxSensedTemp < temp) =>
          (nodePosition, temp, howMay + me)

        case ((maxSensedPoint, maxSensedTemp, howMay), (_, temp, me)) if (temp > maxTemperature) =>
          (maxSensedPoint, maxSensedTemp, howMay + me)

        case (acc, _) => acc
      }
    }

    def isDanger(data : (P, Double, Int)) : Boolean = data._3 > droneCountToAlert

    val leader = branch(node.has("station")) { S(grain, nbrRange) } { false }

    val distanceToLeader = distanceTo(leader)

    val highestTemperature = C[Double, (P, Double, Int)](
      potential = distanceToLeader,
      acc = aggregationStrategy,
      local = (position, temperature, 1),
      Null = (Point3D(0.0, 0.0, 0.0), 0.0, 0)
    )

    val normalized = highestTemperature match {
      case (_, _, 0) => highestTemperature
      case (target, temperature, howMany) => (target, temperature / howMany, howMany)
    }

    val leaderPosition = broadcast(leader, position)
    val goalPosition = broadcast(leader, mux(isDanger(normalized))(normalized._1)(position))
    val danger = G[Boolean](leader, isDanger(normalized), a => a, nbrRange)

    val id = broadcast(leader, mid())


    node.put("center", goalPosition)
    node.put("leader_id", id)
  }

}
