package org.autotdd.engine

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EngineDefaultTests extends EngineStringStringTests {

  "An engine" should "throw Exception if second scenario has because clause that matches first" in {
    val bldr = builderWithUseCase.
      scenario("A").expected("X").
      scenario("AB").because("A").expected("Y");
    val e = evaluating { bldr.build } should produce[ScenarioConflictException]
  }

  it should "throw exception is default condition matches because" in {
    val bldr = builderWithUseCase.
      scenario("C").expected("X").
      scenario("B").because("B").expected("Y").
      scenario("C").because("C").expected("Z")
    val e = evaluating { bldr.build } should produce[ScenarioConflictException]
    val defaultScenario = bldr.scenariosForBuild(0)
    val lastScenario = bldr.scenariosForBuild(2)
    assertEquals(defaultScenario, e.scenario)
    assertEquals(lastScenario, e.beingAdded)
  }

  it should "Allow a default value to be specified" in {
    val e = builderWithUseCase.withDefaultCode((s: String) => "default").build;
    assertEquals("default", e("X"))
  }
  it should "Allow a default value to be specified with scenarios" in {
	  val e = builderWithUseCase.withDefaultCode((s: String) => "default").
	   scenario("A").expected("X").because("A").
      scenario("AB").because("B").expected("Y").
      build
	  assertEquals("default", e("X"))
	  assertEquals("X", e("A"))
	  assertEquals("X", e("AQ"))
	  assertEquals("Y", e("AB"))
  }

  it should "Allow throw exception if second default value is specified" in {
    val bldr = builderWithUseCase.withDefaultCode((s: String) => "default")
    val e = evaluating { bldr.withDefaultCode((s: String) => "default2") } should produce[CannotDefineDefaultTwiceException]
    assertEquals("Original Code:\nCodeFn(((s: String) => \"default\"))\nBeingAdded\nCodeFn(((s: String) => \"default2\"))", e.getMessage())
  }

  //    assertEquals("X", e("A"))
  //    assertEquals("Y", e("B"))
  //    assertEquals("Z", e("C"))
  //    assertEquals("X", e("D"))
  //    println(e)

}