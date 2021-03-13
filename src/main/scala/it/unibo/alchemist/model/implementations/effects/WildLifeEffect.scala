package it.unibo.alchemist.model.implementations.effects

import it.unibo.alchemist.boundary.gui.effects.Effect
import it.unibo.alchemist.boundary.wormhole.interfaces.IWormhole2D
import it.unibo.alchemist.model.implementations.effects.WildLifeEffect._
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.implementations.nodes._
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.AwtShapeCompatible
import it.unibo.alchemist.model.interfaces.{ Environment, Node, Position2D }
import it.unibo.scafi.space.Point3D

import java.awt._
import java.awt.geom._
import scala.collection.mutable

/**
 * ad-hoc effect to draw drones, animal and station.
 */
class WildLifeEffect extends Effect {

  override def apply[T, P <: Position2D[P]](
    g: Graphics2D,
    node: Node[T],
    env: Environment[T, P],
    wormhole: IWormhole2D[P]
  ): Unit =
    env match {
      case env: EuclideanPhysics2DEnvironment[T] if env.getNodes.contains(node) =>
        val nodePosition: P = env.getPosition(node).asInstanceOf[P]
        val viewPoint: Point = wormhole.getViewPoint(nodePosition)
        val manager = new SimpleNodeManager[T](node)
        val (x, y) = (viewPoint.x, viewPoint.y)
        node match {
          case animal: Animal2D[T]     => drawAnimal(g, animal, x, y, env, wormhole.getZoom)
          case _: LoraStation[T, P]    => drawStation(g, node, x, y, env, wormhole.getZoom)
          case mobile: MobileNode2D[T] => drawDrone(g, mobile, x, y, env, wormhole.getZoom)
          case _                       =>
        }
      case _ =>
    }

  override def getColorSummary: Color = Color.GREEN

  def drawDrone[T](
    g: Graphics2D,
    droneNode: MobileNode2D[T],
    x: Int,
    y: Int,
    env: EuclideanPhysics2DEnvironment[T],
    zoom: Double
  ): Unit = {
    val transform = getTransform(x, y, zoom * DRONE_SIZE, rotation(droneNode))
    val manager = new SimpleNodeManager[T](droneNode)
    val shape =
      if (manager.has("type") && manager.get[String]("type") == "healer")
        HEALER_SHAPE
      else
        DRONE_SHAPE
    val transformedShape = transform.createTransformedShape(shape)
    if (manager.has("target")) {
      val crossShape = transform.createTransformedShape(HEALER_TASK)
      g.setColor(Color.GREEN)
      g.fill(crossShape)
    }
    g.setColor(getColorFromLeader(manager).getOrElse(DRONE_COLOR))
    g.fill(transformedShape)
  }

  def drawAnimal[T](
    g: Graphics2D,
    node: Animal2D[T],
    x: Int,
    y: Int,
    env: EuclideanPhysics2DEnvironment[_],
    zoom: Double
  ): Unit = {
    val animal = env.getShapeFactory.circleSector(ANIMAL_SIZE, Math.PI, 0)
    val danger = env.getShapeFactory.circle(ANIMAL_SIZE)
    val manager = new SimpleNodeManager[T](node)
    val transform = getTransform(x, y, zoom, 0.0)
    (animal, danger) match {
      case (animal: AwtShapeCompatible, danger: AwtShapeCompatible) =>
        val transformedAnimal = transform.createTransformedShape(animal.asAwtShape())
        val transformedDanger = transform.createTransformedShape(danger.asAwtShape())
        if (manager.get[Boolean]("danger")) {
          g.setColor(Color.RED)
          g.fill(transformedDanger)
        }
        g.setColor(ANIMAL_COLOR_CACHE.getOrElseUpdate(node.group, randomColor()))
        g.fill(transformedAnimal)
      case _ =>
    }
  }

  def drawStation[T](
    g: Graphics2D,
    node: Node[T],
    x: Int,
    y: Int,
    env: EuclideanPhysics2DEnvironment[_],
    zoom: Double
  ): Unit = {
    val station = STATION_SHAPE
    val manager = new SimpleNodeManager[T](node)
    val transform = getTransform(x, y, zoom * STATION_SIZE, 0.0)
    val areaTransform = getTransform(x, y, zoom, 0.0)
    val influence = Some(manager).collect { case node if node.has("influence") => node.get[Double]("influence") }
      .map(env.getShapeFactory.circle(_))
      .map { case area: AwtShapeCompatible => areaTransform.createTransformedShape(area.asAwtShape()) }
    val transformed = transform.createTransformedShape(station)
    val color = getColorFromLeader(manager).getOrElse(STATION_COLOR)
    g.setColor(color)
    g.fill(transformed)
    for (shape <- influence) {
      g.setColor(new Color(color.getRed, color.getGreen, color.getBlue, STATION_ALPHA))
      g.fill(shape)
    }

  }

  private def rotation[T](node: MobileNode2D[T]): Double = {
    val nodeManager = new SimpleNodeManager[T](node)
    val (dx, dy) = if (nodeManager.has("trajectory")) {
      val trajectory = nodeManager.get[Point3D]("trajectory")
      (trajectory.x, trajectory.y)
    } else
      (node.velocity.getX, node.velocity.getY)
    math.atan2(dx, dy)
  }

  private def getTransform(x: Int, y: Int, zoom: Double, rotation: Double): AffineTransform = {
    val transform = new AffineTransform()
    transform.translate(x, y)
    transform.scale(zoom, zoom)
    transform.rotate(rotation)
    transform
  }

  private def getColorFromLeader(node: NodeManager): Option[Color] =
    Some(node).collect { case node if node.has("leader_id") => node.get[Int]("leader_id") }
      .map(colorFromId)

  private def colorFromId(id: Int): Color = PALETTE(id % MAX_COLOR)

}

object WildLifeEffect {

  val MAX_COLOR: Int = 100

  val TRANSPARENT = new Color(255, 255, 255, 0)

  val PALETTE: Map[Int, Color] = (0 to 100).map(_ -> randomColor).toMap

  val ANIMAL_COLOR_CACHE: mutable.Map[String, Color] = new mutable.HashMap()

  val DRONE_SHAPE: Polygon = new Polygon(Array(-2, 0, 2), Array(0, -5, 0), 3)

  val HEALER_SHAPE: Area = {
    val area = new Area(DRONE_SHAPE)
    val circle = new Area(new Ellipse2D.Double(-1, -1, 2, 2))
    area.subtract(circle)
    area
  }

  val HEALER_TASK: Area = {
    val horizontal = new Area(new Rectangle2D.Double(-1.5, 0.5, 3, 1))
    val vertical = new Area(new Rectangle2D.Double(-0.5, -0.5, 1, 3))
    horizontal.add(vertical)
    horizontal
  }

  val STATION_SHAPE: Shape = {
    val circle = new Area(new Ellipse2D.Double(-1, -1, 2, 2))
    val innerCircle = new Area(new Ellipse2D.Double(-.5, -.5, 1, 1))
    val area = new Area(new Arc2D.Double(-1.5, -1.5, 3, 3, 30, 120, Arc2D.PIE))
    area.subtract(circle)
    area.add(innerCircle)
    area
  }

  val STATION: SimpleMolecule = new SimpleMolecule("station")

  val STATION_SIZE = 24.0

  val STATION_ALPHA = 30

  val DRONE_SIZE = 6.0

  val ANIMAL_SIZE = 12.0

  val DRONE_COLOR: Color = Color.BLACK

  val STATION_COLOR: Color = Color.RED

  def randomColor(): Color = new Color(Color.HSBtoRGB(math.random() floatValue (), 0.5f, 0.5f))

}
