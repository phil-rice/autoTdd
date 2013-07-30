package org.autotdd.example

import org.autotdd.engine.Engine

object HelloWorld {

  val engine = Engine[Int, String]().
    useCase("Returns hello world the requested number of times").
    scenario(1, "Just once").
    expected("Hello World").
    code((i: Int) => List.fill(i)("Hello World").mkString(", ")).
    scenario(2, "Two times").
    expected("Hello World, Hello World").
    build;

  def main(args: Array[String]) {
    println(engine(1))
    println(engine(2))
    println(engine(3))
  }
}