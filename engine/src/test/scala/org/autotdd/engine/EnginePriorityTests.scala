package org.autotdd.engine

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EnginePriorityTests extends EngineStringStringTests {

  "An Engine" should "allow the priority to set the rules order" in {
    def build(a: Int, b: Int, c: Int) =
      builder.useCase("U").scenario("X").expected("FromX").
        scenario("A").because("A").expected("FromA").priority(a).
        scenario("B").because("B").expected("FromB").priority(b).
        scenario("C").because("C").expected("FromC").priority(c).
        build
    assertEquals("if(A)\n FromA:U[1]\nelse\n if(B)\n  FromB:U[2]\n else\n  if(C)\n   FromC:U[3]\n  else\n   FromX:U[0]\n", build(1,2,3).toString)
    assertEquals("if(C)\n FromC:U[3]\nelse\n if(B)\n  FromB:U[2]\n else\n  if(A)\n   FromA:U[1]\n  else\n   FromX:U[0]\n", build(3,2,1).toString)
  }
}