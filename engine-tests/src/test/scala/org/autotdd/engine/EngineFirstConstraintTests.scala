package org.autotdd.engine

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.autotdd.constraints._
import org.autotdd.engine._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class EngineFirstConstraintTests extends EngineStringStringTests {

  "An empty engine" should "change from root to if then with one constraint" in {
    val scenario = Scenario("A").produces("X").becauseBecause("A")
    val engine = Engine1("Z", UseCase("", scenario))
    assertEngineMatches(engine, Right(Node(because = "A", inputs = List("A"), yes = Left(("X": Code).copy(constraints = List(scenario))), no = Left("Z": Code), constraintThatCausedNode = scenario)))
    checkConstraintsExist(engine, "A");
  }

  it should "Add constraint to root if adding assertion" in {
    val scenario = Scenario("A").becauseBecause("A").produces(("Z"))
    val engine = Engine1("Z", UseCase("", scenario))
    assertEngineMatches(engine, Left(("Z": Code).copy(constraints = List(scenario))))
  }

  it should "Throw NoExpectedException if constraint doesnt have expected" in {
    evaluating { Engine1("Z", UseCase("", Scenario("A").becauseBecause("A").byCallingCode("X"))) } should produce[NoExpectedException]
    evaluating { Engine1("Z", UseCase("", Scenario("A").byCallingCode("X"))) } should produce[NoExpectedException]

  }

  it should "Throw AssertionException if first constraint is assertion and comes to wrong result" in {
    evaluating { Engine1[String, String]("Z", UseCase("", Scenario("A").produces("X"))) } should produce[AssertionException]
  }
  it should "Throw ConstraintBecauseException if because is not true in scenario" in {
	  evaluating { Engine1[String, String]("Z", UseCase("", Scenario("A").produces("X").becauseBecause("B"))) } should produce[ConstraintBecauseException]
  }

  
}