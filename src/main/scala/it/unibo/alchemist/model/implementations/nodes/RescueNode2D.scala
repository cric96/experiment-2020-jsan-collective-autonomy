package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment

/**
 * node that rescue animal
 */
class RescueNode2D[T](env: Environment[T, Euclidean2DPosition], maximumSpeed: Double = 1.0)
    extends MobileNode2D[T](env, maximumSpeed, "drone")
