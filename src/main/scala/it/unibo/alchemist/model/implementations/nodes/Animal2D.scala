package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment

/**
 * it marks a node as animal.
 */
class Animal2D[T](env: Environment[T, Euclidean2DPosition], maximumSpeed: Double = 1.0, group: String)
    extends MobileNode2D[T](env, maximumSpeed, group)
