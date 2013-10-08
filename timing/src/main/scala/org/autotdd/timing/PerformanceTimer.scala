package org.autotdd.timing

import org.autotdd.carers._

trait PerformanceTimer {

  def nanos: Long
  def getDataItem(i: Int): List[Any]
  def call(params: List[Any])

  def run(count: Int) = {
    var sum: Long = 0
    for (i <- 0 to count) {
      val start = nanos
      val params = getDataItem(i)
      call(params)
      val duration = nanos - start
      sum += duration
    }
    sum / 1000.0 / count
  }
  def warmUp(count: Int) =
    run(count)
  def time(count: Int) =
    run(count)

}

trait SystemPerformanceTimer {
  def nanos = System.nanoTime()
}

class CarersPerformanceTimer extends PerformanceTimer with SystemPerformanceTimer {
  final val engine = Carers.engine

  def getDataItem(i: Int): List[Any] = List(CarersXmlSituation(World("2010-6-9"), Xmls.validateClaim("CL100104A")))
  def call(params: List[Any]) {
    engine(params(0).asInstanceOf[CarersXmlSituation])
  }
}

object CarersTimer {
  def main(args: Array[String]) {
    val carers = new CarersPerformanceTimer
    carers.warmUp(1000)
    println(f"${carers.time(10000)} us/situation")
  }

}