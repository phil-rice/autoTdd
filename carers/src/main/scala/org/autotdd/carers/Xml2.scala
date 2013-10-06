package org.autotdd.carers

import org.autotdd.engine.Path
import scala.xml._

case class Fold[T, A](initial: A, fn: (A, T) => A)

trait FragStrategy[S, Result] {
  def findResult(raw: S, paths: List[Path]): Result
  def findA[T, A](result: Result, convertor: (Result) => T, fold: Fold[T, A]): A
}

case class Frag[S, Result, T, A](fragStrategy: FragStrategy[S, Result], raw: S, paths: List[Path] = List(), convertor: (Result) => T = (n: Result) => throw new IllegalStateException, fold: Option[Fold[T, A]] = None) {
  def \(s: String) = Frag(fragStrategy, raw, Path(true, s) :: paths, convertor, fold)
  def \\(s: String) = Frag(fragStrategy, raw, Path(false, s) :: paths, convertor, fold)
  def \[T](c: (Result) => T) = Frag[S, Result, T, T](fragStrategy, raw, paths, c, None)
  def \[A](fold: Fold[T, A]) = Frag[S, Result, T, A](fragStrategy, raw, paths, convertor, Some(fold))

  lazy val nodes = fragStrategy.findResult(raw, paths)
  lazy val value = fold match {
    case Some(f) => fragStrategy.findA(nodes, convertor, f) //
    case _ => convertor(nodes).asInstanceOf[A] // Can only be here without Fold, in which case T = A
  }
  def apply(): A = value
}

object Xml {
  class XmlFragStrategy extends FragStrategy[Elem, NodeSeq] {
    def findResult(raw: Elem, paths: List[Path]) = paths.reverse.foldLeft(raw.asInstanceOf[NodeSeq])((acc, p) => p.linked match {
      case true => acc \ p.element
      case false => acc \\ p.element
    })
    def findA[T, A](result: NodeSeq, convertor: (NodeSeq) => T, fold: Fold[T, A]): A = result.map(convertor).foldLeft(fold.initial)(fold.fn)
  }

  val xmlFragStrategy = new XmlFragStrategy();

  def xml(x: Elem): Frag[Elem, NodeSeq, _, _] = Frag(xmlFragStrategy, x)
  def string = (n: NodeSeq) => n.text
  def integer = (n: NodeSeq) => n.text.toInt
  def double = (n: NodeSeq) => n.text.toDouble
  def nodeSeq = (n: NodeSeq) => n
  def obj [A](fn: (NodeSeq ) => A) = (n: NodeSeq) => fn(n)

  def main(args: Array[String]) {
    val x = <x>
              <a>1</a>
              <a>2</a>
              <a>3</a>
              <b>1</b>
            </x>
    val f1 = xml(x) \ "b" \ integer
    val f2 = xml(x) \ "a" \ integer \ Fold[Int, Int](0, (a, b) => a + b)
    println(f1())
    println(f2())
  }
}