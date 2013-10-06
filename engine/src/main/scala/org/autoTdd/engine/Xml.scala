package org.autotdd.engine

import scala.xml.NodeSeq
import scala.xml.Elem
import org.joda.time.DateTime
import scala.concurrent.stm._
import org.joda.time.format.DateTimeFormat

//object XmlFragment {
//  def integer(e: Elem) = new XmlFragment[Integer](e, (_, r) => try { Some(Integer.parseInt(r)) } catch { case e: Throwable => None })
//  def double(e: Elem) = new XmlFragment[Double](e, (_, r) => try { Some(java.lang.Double.parseDouble(r)) } catch { case e: Throwable => None })
//
//  def string(e: Elem) = new XmlFragment[String](e, (_, r) => Some(r), None)
//  def string(e: Elem, default: String) = new XmlFragment[String](e, (_, r) => Some(r), Some(default))
//  private def string(e: Elem, default: Option[String]) = new XmlFragment[String](e, (_, r) => Some(r), default)
//
//  def yesNo(e: Elem): XmlFragment[Boolean] = yesNo(e, None)
//  def yesNo(e: Elem, default: Boolean): XmlFragment[Boolean] = yesNo(e, Some(default))
//  private def yesNo(e: Elem, default: Option[Boolean]): XmlFragment[Boolean] =
//    new XmlFragment[Boolean](e, (_, r) =>
//      r match {
//        case "yes" => Some(true)
//        case "no" => Some(false)
//        case _ => None
//      }, default)
//
//  def date(e: Elem): XmlFragment[DateTime] = date("yyyy-MM-dd", e)
//  def date(pattern: String, e: Elem): XmlFragment[DateTime] = new XmlFragment[DateTime](e, (_, r) =>
//    try { Some(DateTimeFormat.forPattern(pattern).parseDateTime(r)) } catch {
//      case e: Throwable => None
//    });
//
//  def nodeSeq(e: Elem) = new XmlFragment[NodeSeq](e, (n, _) => Some(n))
//  def obj[T](e: Elem, fn: (NodeSeq, String) => Option[T]) = new XmlFragment[T](e, fn)
//}

object Xml {
  type XmlFragment[T, A] = Fragment[Elem, NodeSeq, T, A]
  class XmlFragStrategy extends FragStrategy[Elem, NodeSeq] {
    def findResult(raw: Elem, paths: List[Path]) = paths.reverse.foldLeft(raw.asInstanceOf[NodeSeq])((acc, p) => p.linked match {
      case true => acc \ p.element
      case false => acc \\ p.element
    })
    def findA[T, A](result: NodeSeq, convertor: (NodeSeq) => Option[T], fold: Fold[T, A]): Option[A] = {
      val converted = result.map(convertor)
      converted.find((f) => f == None) match {
        case Some(thereIsANone) => None
        case _ => Some(converted.collect { case Some(x) => x }.foldLeft(fold.initial)(fold.fn))
      }
    }

  }

  val xmlFragStrategy = new XmlFragStrategy();

  def xml(x: Elem): Fragment[Elem, NodeSeq, _, _] = Fragment(xmlFragStrategy, x)

  def date: (NodeSeq) => Option[DateTime] = date("yyyy-MM-dd")
  def date(pattern: String) = (n: NodeSeq) =>
    try { Some(DateTimeFormat.forPattern(pattern).parseDateTime(n.text)) } catch {
      case e: Throwable => None
    };

  def string = (n: NodeSeq) => Some(n.text)
  def integer = (n: NodeSeq) => Some(n.text.toInt)
  def double = (n: NodeSeq) => Some(n.text.toDouble)
  def nodeSeq = (n: NodeSeq) => Some(n)
  def yesNo: (NodeSeq) => Option[Boolean] = (n: NodeSeq) => n.text match {
    case "yes" => Some(true)
    case "no" => Some(false)
    case _ => None
  }
  def yesNo(default: Boolean): (NodeSeq) => Option[Boolean] = (n: NodeSeq) => n.text match {
    case "yes" => Some(true)
    case "no" => Some(false)
    case "" => Some(default)
    case _ => None
  }
  def obj[A](fn: (NodeSeq) => A) = (n: NodeSeq) => Some(fn(n))

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
trait XmlSituation extends Structure[Elem, NodeSeq] {

  type XmlFragment = Fragment[Elem, NodeSeq, _, _]
  private lazy val fragmentFields = new FieldSet[XmlFragment](this, classOf[XmlFragment])
  private lazy val xmlFields = new FieldSet[Elem](this, classOf[Elem])
  protected lazy val pathMap = PathMap[Elem, NodeSeq](fragmentFields.values)

  protected lazy val fragmentsToString = findFragmentsToString(fragmentFields.fieldMap, (e) => e.mkString(","))
  protected lazy val xmlsToString = structuresToString(pathMap, (s) => "  Xml: " + xmlFields.findFieldWithValue(s).getName() + "  " + s.toString.replace('\n', ' '))

  override def toString() = {
    getClass.getSimpleName() + s"(\n  ${fragmentsToString}\n${xmlsToString})"
  }
}