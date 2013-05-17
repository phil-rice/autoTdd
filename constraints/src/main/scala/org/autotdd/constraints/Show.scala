package org.autotdd.constraints
import scala.reflect.macros.Context
import scala.language.experimental.macros
import scala.reflect.runtime.universe._
object Show {

  def main(args: Array[String]){
    println( showRaw(reify{ new Object}.tree))
  }
  
}