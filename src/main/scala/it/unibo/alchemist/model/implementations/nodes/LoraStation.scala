package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.interfaces.{ Environment, Position }

/**
 * @param env
 * @tparam T
 * @tparam P
 */
class LoraStation[T, P <: Position[P]](env: Environment[T, P]) extends ScafiNode[T, P](env)
