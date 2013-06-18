package org.autotdd.engine

import org.autotdd.constraints._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class EngineExampleTests extends EngineTests[Int] with Engine2Types[List[Int], Int, Int] {

  "The example get engine" should "build" in {
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
  }
}