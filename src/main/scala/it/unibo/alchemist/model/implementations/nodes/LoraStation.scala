package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.interfaces.{Environment, Position}

class LoraStation[T, P <: Position[P]](env : Environment[T, P]) extends ScafiNode[T, P](env)
