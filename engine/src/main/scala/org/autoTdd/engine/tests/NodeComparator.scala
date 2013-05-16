package org.autoTdd.engine.tests


import org.autotdd.constraints.CodeFn
import java.text.MessageFormat
import org.autoTdd.engine.Engine1Types
import org.autoTdd.engine.EngineTypes
import org.autoTdd.engine.Engine2Types

object NodeComparator {
  def comparator1[P, R] = new NodeComparator[R] with Engine1Types[P, R]
  def comparator2[P1, P2, R] = new NodeComparator[R] with Engine2Types[P1, P2, R]
}

trait NodeComparator[R] extends EngineTypes[R] {

  def compare(n1: RorN, n2: RorN): List[String] =
    compare("", n1, n2)

  def compare(prefix: String, n1: RorN, n2: RorN): List[String] = {
    n1 match {
      case Left(result1) =>
        n2 match {
          case Left(result2) => compareResults(prefix, result1, result2)
          case Right(node2) => List(prefix + "left is result " + result1.description + " Right is tree " + node2)
        }
      case Right(node1) =>
        n2 match {
          case Left(result2) => List(prefix + "left is tree " + node1 + " Right is result " + result2.description)
          case Right(node2) => compare(prefix, node1, node2)
        }
    }
  }
  def compareResults(prefix: String, c1: Code, c2: Code): List[String] = {
    check(prefix + "result {0} {1}", c1.description, c2.description) ++
      compareConstraints(prefix + "constraints/", c1.constraints, c2.constraints)
  }
  def compare(prefix: String, n1: N, n2: N): List[String] = {
    check(prefix + "because {0} {1}", n1.because.becauseString, n2.because.becauseString) ++
      check(prefix + "inputs {0} {1}", n1.inputs, n2.inputs) ++
      compare(prefix + "yes/", n1.yes, n2.yes) ++
      compare(prefix + "no/", n1.no, n2.no)
  }

  def compareConstraints(prefix: String, c1s: List[C], c2s: List[C]): List[String] = {
    val sizeMismatch = c1s.size != c2s.size match { case true => List(prefix + " sizes " + c1s.size + "," + c2s.size); case _ => List() };
    sizeMismatch ++ (c1s, c2s).zipped.flatMap((c1, c2) => c1 != c2 match { case true => compareConstraint(prefix +"["+c1.becauseString +"]", c1, c2); case _ => List() });
  }

  def compareConstraint(prefix: String, c1: C, c2: C): List[String] = {
    val b = c1.becauseString != c2.becauseString match { case true => List(prefix + "because " + c1.becauseString + ", " + c2.becauseString); case _ => List() }
    val i = c1.params != c2.params match { case true => List(prefix + "params " + c1.params + ", " + c2.params); case _ => List() }
    val e = c1.expected != c2.expected match { case true => List(prefix + "expected " + c1.expected + ", " + c2.expected); case _ => List() }
    val c = c1.code.description != c2.code.description match { case true => List(prefix + "code " + c1.code.description + ", " + c2.code.description); case _ => List() }
    b ++ i ++ e ++ c
  }
  def compareSize(prefix: String, c1s: List[C], c2s: List[C]): List[String] = {
    if (c1s.size != c2s.size)
      List(prefix + " sizes " + c1s.size + "," + c2s.size);
    else
      List()
  }

  def check[T <: AnyRef](pattern: String, t1: T, t2: T): List[String] = {
    if (t1 == t2)
      List()
    else
      List(MessageFormat.format(pattern, t1, t2))
  }

} 