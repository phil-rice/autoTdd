package org.autotdd.example

import org.junit.runner.RunWith

import org.autotdd.engine._
import org.autotdd.constraints._
import org.autotdd.engine.tests._

@RunWith(classOf[AutoTddRunner])
class Hello2Test {

  val get = Engine[List[Int], Int, Int]((rolls: List[Int], i: Int) => rolls(i),
    List(UseCase("Getting when index in range returns ith item", List(7, 10, 4, 3), 0).produces(7),
      UseCase("", List(7, 10, 4, 3), 1).produces(10),
      UseCase("Getting when index is negative produces 0", List(7, 10, 4, 3), -1).produces(0).because((rolls, i) => i < 0),
      UseCase("Getting when index is too big produces 0", List(7, 10, 4, 3), 4).produces(0).because((rolls, i) => i >= rolls.size),
      UseCase("", List(7, 10, 4, 3), 5).produces(0)));

  val makeFrame = Engine[List[Int], Int, Frame]((rolls: List[Int], i: Int) => NormalFrame(get(rolls, i), get(rolls, i + 1)),
    List(
      UseCase("Normal Frames are when less than 10 pins are knocked down", List(7, 2, 5, 5, 3, 0, 10, 2, 4), 0).produces(NormalFrame(7, 2)),
      UseCase("", List(7, 2, 5, 5, 3, 0, 10, 2, 4), 4).produces(NormalFrame(3, 0)),
      UseCase("Strike Frames are when the first ball knocks down 10", List(7, 2, 5, 5, 3, 0, 10, 2, 4), 6).
        produces(StrikeFrame(10, 2, 4)).
        byCalling((rolls, i) => StrikeFrame(rolls(i), get(rolls, i + 1), get(rolls, i + 2))).
        because(get(rolls, i) == 10),
      UseCase("", List(10, 2), 0).produces(StrikeFrame(10, 2, 0)),
      UseCase("SpareFrames are when the sum of the two balls knock down 10", List(7, 2, 5, 5, 3, 0, 10, 2, 4), 6).produces(SpareFrame(5, 5, 3)).
        because((rolls, i) => get(rolls, i) + get(rolls, i + 1) == 10).
        byCalling((rolls, i) => get(rolls, i) + get(rolls, i + 1) == 10),
      UseCase("", List(5, 5), 0).produces(StrikeFrame(5, 5, 0)),
      UseCase("", List(5, 5), 0).produces(SpareFrame(5, 5, 0))));

}