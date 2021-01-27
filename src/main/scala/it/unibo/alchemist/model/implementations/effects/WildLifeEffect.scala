package it.unibo.alchemist.model.implementations.effects

import it.unibo.alchemist.boundary.gui.effects.Effect
import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D
import it.unibo.alchemist.model.implementations.effects.WildLifeEffect._
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.implementations.nodes.{DroneNode2D, MobileNode2D, SimpleNodeManager}
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.AwtShapeCompatible
import it.unibo.alchemist.model.interfaces.{Environment, Node, Position2D}

import java.awt._
import java.awt.geom.AffineTransform
class WildLifeEffect extends Effect {
  override def apply[T, P <: Position2D[P]](g: Graphics2D, node: Node[T], env: Environment[T, P], wormhole: IWormhole2D[P]): Unit = {
    env match {
      case env : EuclideanPhysics2DEnvironment[T] if env.getNodes.contains(node) =>
        val nodePosition : P = env.getPosition(node).asInstanceOf[P]
        val viewPoint : Point = wormhole.getViewPoint(nodePosition)
        val (x, y) = (viewPoint.x, viewPoint.y)
        node match {
          case drone : DroneNode2D[T] => drawDrone(g, drone, x, y, wormhole.getZoom)
          case _ => drawStation(g, node, x, y, env, wormhole.getZoom)
        }
      case _ =>
    }
  }
  override def getColorSummary: Color = Color.GREEN
  def drawDrone[T](g : Graphics2D, droneNode : MobileNode2D[T], x : Int, y : Int, zoom : Double): Unit = {
    val transform = getTransform(x, y, zoom * DRONE_SIZE, rotation(droneNode))
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
    val station = env.getShapeFactory.circle(STATION_SIZE)
    val manager = new SimpleNodeManager[T](node)
    val transform = getTransform(x, y, zoom, 0.0)
    station match {
      case station : AwtShapeCompatible => val transformed = transform.createTransformedShape(station.asAwtShape())
        if(manager.has("leader_id")) {
          g.setColor(colorFromId(manager.get[Int]("leader_id")))
        } else {
          g.setColor(STATION_COLOR)
        }
        g.fill(transformed)
    }
  }

  private def rotation(node : MobileNode2D[_]) : Double = {
    val direction = node.velocity
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

object WildLifeEffect {
  val MAX_COLOR : Int = 100
  val TRANSPARENT = new Color(255, 255, 255, 0)
  val PALETTE : Map[Int, Color] = (0 to 100).map(_ -> new Color(Color.HSBtoRGB(math.random() floatValue(), 0.5f, 0.5f))).toMap
  val DRONE_SHAPE : Polygon = new Polygon(Array(-2, 0, 2), Array(0, - 5, 0), 3)
  val DRONE : SimpleMolecule = new SimpleMolecule("drone")
  val STATION : SimpleMolecule = new SimpleMolecule("station")
  val STATION_SIZE = 4.0
  val DRONE_SIZE = 1.5
  val DRONE_COLOR : Color = Color.BLACK
  val STATION_COLOR : Color = Color.RED
  def colorsFromAmplitude(amplitude : Double) : Array[Color] = {
    val alpha = ((amplitude + 25) / 100.0 * 255).toInt
    val normalizedAlpha = if(alpha > 255) 255 else alpha
    val RED_ALPHA = new Color(255, 0, 0, normalizedAlpha)
    val ORANGE_ALPHA = new Color(Color.ORANGE.getRed, Color.ORANGE.getGreen, Color.ORANGE.getBlue, normalizedAlpha)
    val YELLOW_ALPHA = new Color(Color.YELLOW.getRed, Color.YELLOW.getGreen, Color.YELLOW.getBlue, normalizedAlpha)
    Array(RED_ALPHA, ORANGE_ALPHA, YELLOW_ALPHA, TRANSPARENT)
  }
}
