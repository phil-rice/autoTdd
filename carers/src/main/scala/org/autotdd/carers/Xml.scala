package org.autotdd.carers

import scala.xml.NodeSeq
import scala.xml.Elem
import org.joda.time.DateTime
import scala.concurrent.stm._
import org.joda.time.format.DateTimeFormat

object XmlFragment {
  def integer(e: Elem) = new XmlFragment[Integer](e, (n) => try { Some(Integer.parseInt(n)) } catch { case e: Throwable => None })
  def double(e: Elem) = new XmlFragment[Double](e, (n) => try { Some(java.lang.Double.parseDouble(n)) } catch { case e: Throwable => None })

  def string(e: Elem) = new XmlFragment[String](e, (n) => Some(n), None)
  def string(e: Elem, default: String) = new XmlFragment[String](e, (n) => Some(n), Some(default))
  private def string(e: Elem, default: Option[String]) = new XmlFragment[String](e, (n) => Some(n), default)

  def yesNo(e: Elem): XmlFragment[Boolean] = yesNo(e, None)
  def yesNo(e: Elem, default: Boolean): XmlFragment[Boolean] = yesNo(e, Some(default))
  private def yesNo(e: Elem, default: Option[Boolean]): XmlFragment[Boolean] =
    new XmlFragment[Boolean](e, (n) =>
      n match {
        case "yes" => Some(true)
        case "no" => Some(false)
        case _ => None
      }, default)

  def date(e: Elem): XmlFragment[DateTime] = date("yyyy-MM-dd", e)
  def date(pattern: String, e: Elem): XmlFragment[DateTime] = new XmlFragment[DateTime](e, (n) =>
    try { Some(DateTimeFormat.forPattern(pattern).parseDateTime(n)) } catch {
      case e: Throwable => None
    });
}

case class Path(linked: Boolean, element: String) {
  override def toString() = (if (linked) """\""" else """\\""") + element
}

trait Fragment[Structure, End, Result, T] {
  type RealFragment <: Fragment[Structure, End, Result, T]
  
  def raw: Structure
  def endToResult: (End) => Result
  def fn: (Result) => Option[T]
  def default: Option[T]
  
  val value = Ref[Option[T]](None)
  val evaluated = Ref[Boolean](false)

  def end(e: Structure): End
  def withPath(path: Path): RealFragment

  def \(s: String) = withPath(Path(true, s))
  def \\(s: String) = withPath(Path(false, s))

  def apply(): T =
    get() match {
      case Some(t) => t;
      case _ => default.get //If you get an exception here, you haven't given a default for this value, and the value is now 'None'
    }

  def get(): Option[T] =
    atomic { implicit txn =>
      if (!evaluated.getAndTransform((t) => true)) {
        val e = end(raw);
        val r = endToResult(e)
        val t = fn(r)
        value.set(t)
      }
      value.get
    }
}

case class XmlFragment[T](raw: Elem, fn: (String) => Option[T], default: Option[T] = None, paths: List[Path] = List()) extends Fragment[Elem, NodeSeq, String, T] {
  def endToResult = (_.text)
  type RealFragment = XmlFragment[T]
  def withPath(path: Path) = XmlFragment[T](raw, fn, default, path :: paths)
  def end(e: Elem): NodeSeq = paths.reverse.foldLeft(e.asInstanceOf[NodeSeq])((acc, p) => p.linked match {
    case true =>
      val r = acc \ p.element
      val t = r.text
      r
    case false => acc \\ p.element
  })
}

trait XmlSituation {

}