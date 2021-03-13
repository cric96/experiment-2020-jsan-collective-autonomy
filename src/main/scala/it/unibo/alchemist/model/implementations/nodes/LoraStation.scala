package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.interfaces.{ Environment, Position }

/**
 * it is used only to mark a node as station
 */
class LoraStation[T, P <: Position[P]](env: Environment[T, P]) extends ScafiNode[T, P](env)
