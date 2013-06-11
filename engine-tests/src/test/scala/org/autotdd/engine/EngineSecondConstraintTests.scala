package org.autotdd.engine

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.autotdd.engine._
import org.autotdd.constraints._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class EngineSecondConstraintTests extends EngineStringStringTests {
  it should "add to no clause if because is false for root" in {
    val a = Scenario("A").becauseBecause("A").produces("X")
    val b = Scenario("B").becauseBecause("B").produces("Y")
    val engine = Engine1[String, String]("Z", UseCase("", a, b));
    assertEngineMatches(engine,
      Right(Node(because = "A", constraintThatCausedNode = a, inputs = List("A"),
        yes = Left(("X": Code).copy(constraints = List(a))),
        no = Right(Node(because = "B", constraintThatCausedNode = b, inputs = List("B"),
          yes = Left(("Y": Code).copy(constraints = List(b))),
          no = Left("Z": Code))))))
    checkConstraintsExist(engine, "A", "B");
  }

  "An  engine" should "add to yes clause if because is true for root" in {
    val a = Scenario("A").becauseBecause("A").produces("X")
    val ab = Scenario("AB").becauseBecause("B").produces("Y")
    val engine = Engine1[String, String]("Z", UseCase("", a, ab));
    assertEngineMatches(engine,
      Right(Node(because = "A", constraintThatCausedNode = a, inputs = List("A"),
        yes = Right(Node(because = "B", constraintThatCausedNode = ab, inputs = List("AB"),
          yes = Left(("Y": Code).copy(constraints = List(ab))),
          no = Left(("X": Code).copy(constraints = List(a))))),
        no = Left("Z": Code))))
    checkConstraintsExist(engine, "A", "B");
  }


  it should "Add assertions to the yes if constraint comes to correct value" in {
    val a = Scenario("A").becauseBecause("A").produces("X")
    val ab = Scenario("AB").produces("X")
    val engine = Engine1[String, String](default = "Z", UseCase("", a, ab))
    assertEngineMatches(engine, Right(Node(because = "A", inputs = List("A"), yes = Left(("X": Code).copy(constraints = List(ab, a))), no = Left("Z": Code), constraintThatCausedNode = a)))

  }

  it should "Add assertions to the no if constraint comes to correct value" in {
    val a = Scenario("A").becauseBecause("A").produces("X")
    val b = Scenario("B").produces("Z")
    val engine = Engine1[String, String]("Z", UseCase("", a, b))
    assertEngineMatches(engine, Right(Node(because = "A", constraintThatCausedNode = a, inputs = List("A"), yes = Left(("X": Code).copy(constraints = List(a))), no = Left(("Z": Code).copy(constraints = List(b))))))
  }

  //TODO Consider how to deal with identical result, different because. It's not clear to me what I should do
  it should "throw exception if  cannot differentiate inputs, identical result, different because" in {
    val xBecauseA = Scenario("AB").becauseBecause("A").produces("X")
    val xbecauseB = Scenario("AB").becauseBecause("B").produces("X")
    evaluating { Engine1[String, String]("Z", UseCase("", xBecauseA, xbecauseB)) } should produce[ConstraintConflictException]
  }

  it should "Throw AssertionException if constraint matches root condition and comes to wrong conclusion" in {
    val a = Scenario("AB").becauseBecause("A").produces("X")
    val comesToDifferentConclusionWithoutBecause = Scenario[String, String]("AB").produces("Z")
    evaluating { Engine1[String, String]("Z", UseCase("", a, comesToDifferentConclusionWithoutBecause)) } should produce[AssertionException]
  }
  it should "Throw AssertionException if constraint doesnt match root condition and comes to wrong conclusion" in {
    val a = Scenario("A").becauseBecause("A").produces("X")
    val b = Scenario("B").produces("X") //should really come to Z as doesn't have because
    evaluating { Engine1[String, String]("Z", UseCase("", a, b)) } should produce[AssertionException]
  }
}