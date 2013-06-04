package org.autotdd.engine

import org.scalatest.FlatSpec

import org.scalatest.matchers.ShouldMatchers
import org.autotdd.constraints._
import org.autotdd.engine._

class EngineFirstConstraintTests extends EngineTests {

  "An empty engine" should "change from root to if then with one constraint" in {
    val useCase = UseCase("A", "A").produces("X").becauseBecause("A")
    val engine = Engine("Z", useCase);
    assertEngineMatches(engine, Right(Node(because = "A", inputs = List("A"), yes = Left(("X": Code).copy(constraints = List(useCase))), no = Left("Z": Code))))
    checkConstraintsExist(engine, "A");
  }

  it should "Add constraint to root if adding assertion" in {
    val useCase = UseCase("A", "A").becauseBecause("A").produces(("Z"))
    val engine = Engine("Z", useCase)
    assertEngineMatches(engine, Left(("Z": Code).copy(constraints = List(useCase))))
  }

  it should "Throw NoExpectedException if constraint doesnt have expected" in {

    evaluating { Engine("Z", UseCase("A", "A").becauseBecause("A").byCallingCode("X")) } should produce[NoExpectedException]
    evaluating { Engine("Z", UseCase[String, String]("A", "A").byCallingCode("X")) } should produce[NoExpectedException]

  }

  it should "Throw AssertionException if first constraint is assertion and comes to wrong result" in {
    evaluating { Engine((p: String) => "Z", List(UseCase[String, String]("A", "X").becauseBecause("A"))) } should produce[ConstraintBecauseException]
  }
}