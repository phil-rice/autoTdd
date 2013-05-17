package org.autotdd.example

import org.junit.runner.RunWith

import org.autotdd.engine.tests.AutoTddRunner
import org.autotdd.engine._

@RunWith(classOf[AutoTddRunner])
class Hello2Test {

  //get returns the ith ball or zero   
  val get = Engine2((rolls: List[Int], i: Int) => rolls(i));

  get constraint (List(7, 10, 4, 3), 0, 7)

  get constraint (List(7, 10, 4, 3), -1, 0, (rolls, i) => i < 0)
  get assertion (List(7, 10, 4, 3), 1, 10)
  get constraint (List(7, 10, 4, 3), 4, 0, (rolls, i) => i >= rolls.length)
  get constraint (List(7, 10, 4, 3), 5, 0)

  val makeFrame = Engine2[List[Int], Int, Frame]((rolls: List[Int], i: Int) => NormalFrame(get(rolls, i), get(rolls, i + 1)))

  makeFrame.constraint(List(7, 2, 5, 5, 3, 0, 10, 2, 4), 0,
    expected = NormalFrame(7, 2))

  makeFrame.constraint(List(7, 2, 5, 5, 3, 0, 10, 2, 4), 6, StrikeFrame(10, 2, 4),
    (rolls, i) => StrikeFrame(rolls(i), get(rolls, i + 1), get(rolls, i + 2)),
    (rolls, i) => get(rolls, i) == 10)

  makeFrame.constraint(List(7, 2, 5, 5, 3, 0, 10, 2, 4), 2, SpareFrame(5, 5, 3),
    (rolls, i) => SpareFrame(get(rolls, i), get(rolls, i + 1), get(rolls, i + 2)),
    (rolls, i) => get(rolls, i) + get(rolls, i + 1) == 10)

  makeFrame.constraint(List(7, 2, 5, 5, 3, 0, 10, 2, 4), 4, NormalFrame(3, 0))

  makeFrame.assertion(List(7), 0, NormalFrame(7, 0))

  makeFrame.assertion(List(5, 5), 0, SpareFrame(5, 5, 0))

  makeFrame.assertion(List(10), 0, StrikeFrame(10, 0, 0))

  makeFrame.assertion(List(10, 2), 0, StrikeFrame(10, 2, 0))

  //    val makeFrames = new FunnyFoldingEngine[Int, Int, Frame]
  //    val x = 0;
  //    makeFrames.constraint(0, Array(7, 2, 5, 5, 3, 0, 10, 2, 4),
  //      (f, acc) => x, Map(
  //        0 -> NormalFrame(7, 2),
  //        1 -> SpareFrame(5, 5, 3),
  //        2 -> NormalFrame(3, 0),
  //        3 -> StrikeFrame(10, 2, 4),
  //        4 -> NormalFrame(2, 4)))

  //    makeFrames.constraint(0, Array(7, 2, 5, 5, 3, 0, 10, 2, 4),
  //      (f, acc) => acc, Map(
  //        0 -> NormalFrame(7, 2),
  //        1 -> SpareFrame(5, 5, 3),
  //        2 -> NormalFrame(3, 0),
  //        3 -> StrikeFrame(10, 2, 4),
  //        4 -> NormalFrame(2, 4)))

}