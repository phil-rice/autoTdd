package org.autotdd.carers

object Gash {
  def main(args: Array[String]) {
    println("Sunday: " + Xmls.asDate("2013-9-8").dayOfWeek().get())
    println("Monday: " + Xmls.asDate("2013-9-9").dayOfWeek().get())
  }
}