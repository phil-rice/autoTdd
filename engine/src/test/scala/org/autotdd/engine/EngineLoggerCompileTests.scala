package org.autotdd.engine

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EngineLoggerCompileTests extends EngineStringStringTests {
  override val logger = new TestLogger()

  "An empty engine" should "Add scenario to root if adding assertion" in {
    val bldr = builder.useCase("UseCase").
      scenario("W").expected("Z").
      scenario("A").expected("Z")
    logger.reset

    bldr.build
    checkMessages("DEBUG Compile() Adding UseCase[0] as new root", "DEBUG Compile() Adding UseCase[1] as extra scenario for Z")
  }

  it should "log the change from null to a root with one scenario" in {
    val b = builder.useCase("UseCase").scenario("W").expected("Z")
    logger.reset
    b.build
    checkMessages("DEBUG Compile() Adding UseCase[0] as new root")
  }

  "An empty engine" should "log the change from null root to if then with two scenarios" in {
    val bldr = builder.useCase("UseCase").
      scenario("W").expected("Z").
      scenario("A").expected("X").because("A")
    logger.reset
    bldr.build
    checkMessages("DEBUG Compile() Adding UseCase[0] as new root", "DEBUG Compile() Adding UseCase[1] as first if then else")
  }

  it should "log adding to no clause if because is false for root" in {
    val bldr = builder.useCase("UseCase").
      scenario("W").expected("Z").
      scenario("A").expected("X").because("A").
      scenario("B").expected("Y").because("B")
    logger.reset
    bldr.build
    checkMessages(
      "DEBUG Compile() Adding UseCase[0] as new root",
      "DEBUG Compile() Adding UseCase[1] as first if then else",
      "DEBUG Compile() Adding UseCase[2] under no of node UseCase[1]")
  }

  it should "log adding to yes clause if because is true for root" in {
    val bldr = builder.useCase("UseCase").
      scenario("W").expected("Z").
      scenario("A").expected("X").because("A").
      scenario("AB").expected("Y").because("B")
    logger.reset
    bldr.build
    checkMessages(
      "DEBUG Compile() Adding UseCase[0] as new root",
      "DEBUG Compile() Adding UseCase[1] as first if then else",
      "DEBUG Compile() Adding UseCase[2] under yes of node UseCase[1]")
  }

  it should "log correctly when three scenarios are added" in {
    val bldr = builder.useCase("UseCase").
      scenario("W").expected("Z").
      scenario("A").expected("X").because("A").
      scenario("B").expected("Y").because("B").
      scenario("AB").expected("Z").because("AB")
    logger.reset
    bldr.build
    checkMessages(
      "DEBUG Compile() Adding UseCase[0] as new root",
      "DEBUG Compile() Adding UseCase[1] as first if then else",
      "DEBUG Compile() Adding UseCase[2] under no of node UseCase[1]",
      "DEBUG Compile() Adding UseCase[3] under yes of node UseCase[1]")
  }
}