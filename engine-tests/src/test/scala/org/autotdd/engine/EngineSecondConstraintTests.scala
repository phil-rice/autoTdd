package org.autotdd.engine

import org.scalatest.FlatSpec

import org.scalatest.matchers.ShouldMatchers
import org.autotdd.engine._
import org.autotdd.constraints._

class EngineSecondConstraintTests extends EngineTests {

  "An  engine" should "add to yes clause if because is true for root" in {
    val a = UseCase("", "A").becauseBecause("A").produces("X")
    val ab = UseCase("", "AB").becauseBecause("B").produces("Y")
    val engine = Engine[String, String]("Z", a, ab);
    assertEngineMatches(engine,
      Right(Node(because = "A", inputs = List("A"),
        yes = Right(Node(because = "B", inputs = List("AB"),
          yes = Left(("Y": Code).copy(constraints = List(ab))),
          no = Left(("X": Code).copy(constraints = List(a))))),
        no = Left("Z": Code))))
    checkConstraintsExist(engine, "A", "B");
  }

  it should "add to no clause if because is false for root" in {
    val a = UseCase("", "A").becauseBecause("A").produces("X")
    val b = UseCase("", "B").becauseBecause("B").produces("Y")
    val engine = Engine[String, String]("Z", a, b);
    assertEngineMatches(engine,
      Right(Node(because = "A", inputs = List("A"),
        yes = Left(("X": Code).copy(constraints = List(a))),
        no = Right(Node(because = "B", inputs = List("B"),
          yes = Left(("Y": Code).copy(constraints = List(b))),
          no = Left("Z": Code))))))
    checkConstraintsExist(engine, "A", "B");
  }

  it should "Add assertions to the yes if constraint comes to correct value" in {
    val a = UseCase("", "A").becauseBecause("A").produces("X")
    val ab = UseCase("", "AB").produces("X")
    val engine = Engine[String, String](default = "Z", a, ab);
    assertEngineMatches(engine, Right(Node(because = "A", inputs = List("A"), yes = Left(("X": Code).copy(constraints = List(ab, a))), no = Left("Z": Code))))

  }

  it should "Add assertions to the no if constraint comes to correct value" in {
    val a = UseCase("", "A").becauseBecause("A").produces("X")
    val b = UseCase("", "B").produces("Z")
    val engine = Engine[String, String]("Z", a, b);
    assertEngineMatches(engine, Right(Node(because = "A", inputs = List("A"),
      yes = Left(("X": Code).copy(constraints = List(a))),
      no = Left(("Z": Code).copy(constraints = List(b))))))
  }

  //TODO Consider how to deal with identical result, different because. It's not clear to me what I should do
  it should "throw exception if  cannot differentiate inputs, identical result, different because" in {
    val xBecauseA = UseCase("", "AB").becauseBecause("A").produces("X")
    val xbecauseB = UseCase("", "AB").becauseBecause("B").produces("X")
    evaluating { Engine[String, String]("Z", xBecauseA, xbecauseB) } should produce[ConstraintConflictException]
  }

  it should "Throw AssertionException if constraint matches root condition and comes to wrong conclusion" in {
    val a = UseCase("", "AB").becauseBecause("A").produces("X")
    val comesToDifferentConclusionWithoutBecause = UseCase[String, String]("", "AB").produces("Z")
    evaluating { Engine[String, String]("Z", a, comesToDifferentConclusionWithoutBecause) } should produce[AssertionException]
  }
  it should "Throw AssertionException if constraint doesnt match root condition and comes to wrong conclusion" in {
    val a = UseCase("", "A").becauseBecause("A").produces("X")
    val b = UseCase("", "B").produces("X") //should really come to Z as doesn't have because
    evaluating { Engine[String, String]("Z", a, b) } should produce[AssertionException]
  }
}