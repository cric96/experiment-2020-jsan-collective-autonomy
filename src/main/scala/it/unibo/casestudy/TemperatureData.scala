package it.unibo.casestudy
case class TemperatureData[P](dronePosition : P, temperature : Double, howMany : Int = 1) {
  def combine(other : TemperatureData[P]) : TemperatureData[P] = this.copy(howMany = other.howMany + howMany)
  def normalize : TemperatureData[P] = howMany match {
    case 0 => this
    case n => this.copy(temperature = temperature / n)
  }
}