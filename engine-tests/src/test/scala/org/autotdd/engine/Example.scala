

package org.autotdd.engine

import org.autotdd.constraints._

import org.autotdd.engine._

object Example {

//  def main(args: Array[String]) {
//    val c = UseCase("I get zero if index < 0", List(7, 2, 3, 4), -1).
//      produces(0).
//      because((rolls, i) => i < 0);
//
//    println("Final Constraint: " + c)
//    println()
//
//    val list = List(7, 2, 3, 4)
//    val engine = Engine((rolls: List[Int], i: Int) => rolls(i),List(
//      UseCase("I get zero if index < 0", list, -1).
//        produces(0).
//        because((rolls, i) => i < 0),
//      UseCase("I get zero if index is off the end", list, 4).
//        produces(0).
//        because((rolls, i) => i >= rolls.size),
//      UseCase("I get the ith value if index is in range", list, 2).
//        produces(3)))
//    for (i <- -1 until 5)
//      println("i: " + i + ": " + engine(list, i))
//     println(engine)
//  }
}