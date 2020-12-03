package it.unibo.alchemist.effect

import it.unibo.alchemist.boundary.gui.effects.Effect
import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D
import it.unibo.alchemist.effect.DroneAndStation._
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.implementations.nodes.SimpleNodeManager
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.AwtShapeCompatible
import it.unibo.alchemist.model.interfaces.{Environment, Node, Position2D}
import it.unibo.alchemist.node.DroneNode2D

import java.awt.geom.AffineTransform
import java.awt.{Color, Graphics2D, Point, Polygon}
class DroneAndStation extends Effect {
  override def apply[T, P <: Position2D[P]](g: Graphics2D, node: Node[T], env: Environment[T, P], wormhole: IWormhole2D[P]): Unit = {
    (env) match {
      case env : EuclideanPhysics2DEnvironment[T] =>
        val nodePosition : P = env.getPosition(node).asInstanceOf[P]
        val viewPoint : Point = wormhole.getViewPoint(nodePosition)
        val (x, y) = (viewPoint.x, viewPoint.y)
        node match {
          case drone : DroneNode2D[T] => drawDrone(g, drone, x, y, env, wormhole.getZoom)
          case _ => drawStation(g, node, x, y, env, wormhole.getZoom)
        }
    }
  }
  override def getColorSummary: Color = Color.GREEN

  def drawDrone[T](g : Graphics2D, droneNode : DroneNode2D[T], x : Int, y : Int, env : EuclideanPhysics2DEnvironment[_], zoom : Double): Unit = {
    val transform = getTransform(x, y, zoom * 1.5, rotation(droneNode, env))
    val transformedShape = transform.createTransformedShape(DRONE_SHAPE)
    val manager = new SimpleNodeManager[T](droneNode)
    if(manager.has("leader_id")) {
      g.setColor(colorFromId(manager.get[Int]("leader_id")))
    } else {
      g.setColor(DRONE_COLOR)
    }
    g.fill(transformedShape)
  }

  def drawStation[T](g : Graphics2D, node : Node[T], x : Int, y : Int, env : EuclideanPhysics2DEnvironment[_], zoom : Double) : Unit = {
    val station = env.getShapeFactory.circle(CIRCLE_SIZE)
    val manager = new SimpleNodeManager[T](node)
    val trasform = getTransform(x, y, zoom, 0.0)
    station match {
      case station : AwtShapeCompatible => val transformed = trasform.createTransformedShape(station.asAwtShape())
        if(manager.has("leader_id")) {
          g.setColor(colorFromId(manager.get[Int]("leader_id")))
        } else {
          g.setColor(STATION_COLOR)
        }
        g.fill(transformed)
    }
  }

  private def rotation(node : DroneNode2D[_], euclideanPhysics2DEnvironment: EuclideanPhysics2DEnvironment[_]) : Double = {
    val direction = node.currentVector
    math.atan2(direction.getX, direction.getY)
  }

  private def getTransform(x : Int, y : Int, zoom : Double, rotation : Double): AffineTransform = {
    val transform = new AffineTransform()
    transform.translate(x, y)
    transform.scale(zoom, zoom)
    transform.rotate(rotation)
    transform
  }

  private def colorFromId(id : Int) : Color = PALETTE(id % MAX_COLOR)
}

object DroneAndStation {
  val MAX_COLOR = 100
  val PALETTE = (0 to 100).map(_ -> new Color(Color.HSBtoRGB(math.random() floatValue(), 0.5f, 0.5f))).toMap
  val DRONE_SHAPE = new Polygon(Array(-2, 0, 2), Array(0, - 5, 0), 3)
  val DRONE = new SimpleMolecule("drone")
  val STATION = new SimpleMolecule("station")
  val CIRCLE_SIZE = 4.0
  val DRONE_COLOR = Color.BLACK
  val STATION_COLOR = Color.RED
}
