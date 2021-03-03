package it.unibo.casestudy
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
//TODO
object CollectiveTask {
  type Task[P <: AggregateProgram, O] = P => O
  case class TaskContinuation[P <: AggregateProgram, O](program : P => O) extends Task[P, O] {
    override def apply(v1: P): O = program(v1)
    def where(spaceRestriction : P => Boolean)(orElse : P => O) : WhereContinuation[P, O] = WhereContinuation[P, O](program, spaceRestriction, orElse)
    def select(id : ID)(orElse : P=> O) : WhereContinuation[P, O] = WhereContinuation(program, p => p.mid() == id, orElse)
    override def toString(): String = "task"
  }

  case class WhereContinuation[P <: AggregateProgram, O](program : P => O, restriction : P => Boolean, orElse : P => O) extends Task[P, O] {
    override def apply(v1: P): O = v1.mux(restriction(v1)) { program(v1) } { orElse(v1) }
    override def toString(): String = "task restricted"
  }
  def task[P <: AggregateProgram, O](logic : P => O) : TaskContinuation[P, O] = TaskContinuation[P, O](logic)
}
