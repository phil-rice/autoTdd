package org.autotdd.example

import org.junit.runner.RunWith

import org.autotdd.constraints._
import org.autotdd.engine._
import org.autotdd.engine.tests._

@RunWith(classOf[AutoTddRunner])
object Bowling {

  //get returns the ith ball or zero   
  val get = Engine2((rolls: List[Int], i: Int) => rolls(i),
    List(
      UseCase("If index in range of list, return ith item",
        Scenario(List(7, 10, 4, 3), 0).produces(7),
        Scenario(List(7, 10, 4, 3), 3).produces(3)),
      UseCase("If index is negative return zero",
        Scenario(List(7, 10, 4, 3), -1).produces(0).
          byCalling((rolls, i) => 0).
          because((rolls, i) => i < 0)),
      UseCase("If index is too high return zero",
        Scenario(List(7, 10, 4, 3), 4).produces(0).
//          byCalling((rolls, i) => 0).
          because((rolls, i) => i >= rolls.size),
        Scenario(List(7, 10, 4, 3), 5).produces(0),
        Scenario(List(7, 10, 4, 3), 100).produces(0))));
  get.validateConstraints
  println(get(List(), 100))

  val makeFrame = Engine2[List[Int], Int, Frame]((rolls: List[Int], i: Int) => NormalFrame(get(rolls, i), get(rolls, i + 1)),
    List(
      UseCase("NormalFrames are produced when the two balls at and after the ith ball don't add up to 10",
        Scenario(List(7, 2, 5, 5, 3, 0, 10, 2, 4), 0).produces(NormalFrame(7, 2)),
        Scenario(List(7, 2, 5, 5, 3, 0, 10, 2, 4), 4).produces(NormalFrame(3, 0))),
      UseCase("Strike Frames are produced when the ith ball equals 10. They include the ith ball, and the next two balls",
        Scenario(List(7, 2, 5, 5, 3, 0, 10, 2, 4), 6).produces(StrikeFrame(10, 2, 4): Frame).
          because((rolls: List[Int], i: Int) => get(rolls, i) == 10).
          byCalling((rolls: List[Int], i: Int) => StrikeFrame(get(rolls, i), get(rolls, i + 1), get(rolls, i + 2))),
        Scenario(List(10), 0).produces(StrikeFrame(10, 0, 0)),
        Scenario(List(10, 10), 0).produces(StrikeFrame(10, 10, 0)),
        Scenario(List(10, 10, 10), 0).produces(StrikeFrame(10, 10, 10))),
      UseCase("Spare Frames are produced when the two balls at and after the ith ball add up to 10. They include the two balls, and the next ball",
        Scenario(List(7, 2, 5, 5, 3, 0, 10, 2, 4), 2).produces(SpareFrame(5, 5, 3): Frame).
          because((rolls: List[Int], i: Int) => get(rolls, i) + get(rolls, i + 1) == 10).
          byCalling((rolls: List[Int], i: Int) => SpareFrame(get(rolls, i), get(rolls, i + 1), get(rolls, i + 2)): Frame),
        Scenario(List(5, 5), 0).produces(SpareFrame(5, 5, 0)))));

  def main(args: Array[String]) {

    println(get.toString)
    println(makeFrame.toString)
  }
}