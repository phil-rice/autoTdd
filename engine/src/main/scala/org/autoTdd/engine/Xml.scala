package org.autotdd.engine

import scala.xml.NodeSeq
import scala.xml.Elem
import org.joda.time.DateTime
import scala.concurrent.stm._
import org.joda.time.format.DateTimeFormat

object XmlFragment {
  def integer(e: Elem) = new XmlFragment[Integer](e, (_, r) => try { Some(Integer.parseInt(r)) } catch { case e: Throwable => None })
  def double(e: Elem) = new XmlFragment[Double](e, (_, r) => try { Some(java.lang.Double.parseDouble(r)) } catch { case e: Throwable => None })

  def string(e: Elem) = new XmlFragment[String](e, (_, r) => Some(r), None)
  def string(e: Elem, default: String) = new XmlFragment[String](e, (_, r) => Some(r), Some(default))
  private def string(e: Elem, default: Option[String]) = new XmlFragment[String](e, (_, r) => Some(r), default)

  def yesNo(e: Elem): XmlFragment[Boolean] = yesNo(e, None)
  def yesNo(e: Elem, default: Boolean): XmlFragment[Boolean] = yesNo(e, Some(default))
  private def yesNo(e: Elem, default: Option[Boolean]): XmlFragment[Boolean] =
    new XmlFragment[Boolean](e, (_, r) =>
      r match {
        case "yes" => Some(true)
        case "no" => Some(false)
        case _ => None
      }, default)

  def date(e: Elem): XmlFragment[DateTime] = date("yyyy-MM-dd", e)
  def date(pattern: String, e: Elem): XmlFragment[DateTime] = new XmlFragment[DateTime](e, (_, r) =>
    try { Some(DateTimeFormat.forPattern(pattern).parseDateTime(r)) } catch {
      case e: Throwable => None
    });

  def nodeSeq(e: Elem) = new XmlFragment[NodeSeq](e, (n, _) => Some(n))
  def obj[T](e: Elem, fn: (NodeSeq, String) => Option[T]) = new XmlFragment[T](e, fn)
}

case class XmlFragment[T](raw: Elem, fn: (NodeSeq, String) => Option[T], default: Option[T] = None, paths: List[Path] = List()) extends Fragment[Elem, NodeSeq, String, T] {
  def endToResult = (_.text)
  type RealFragment = XmlFragment[T]
  def withPath(path: Path) = XmlFragment[T](raw, fn, default, path :: paths)
  def end(e: Elem, paths: List[Path]): NodeSeq = paths.reverse.foldLeft(e.asInstanceOf[NodeSeq])((acc, p) => p.linked match {
    case true => acc \ p.element
    case false => acc \\ p.element
  })
}

trait XmlSituation extends Structure[Elem, NodeSeq, String] {

  private lazy val fragmentFields = new FieldSet[XmlFragment[_]](this, classOf[XmlFragment[_]])
  private lazy val xmlFields = new FieldSet[Elem](this, classOf[Elem])
  protected lazy val pathMap = PathMap[Elem, NodeSeq, String](fragmentFields.values)

  protected lazy val fragmentsToString = findFragmentsToString(fragmentFields.fieldMap, (e) => e.mkString(","))
  protected lazy val xmlsToString = structuresToString(pathMap, (s) => "  Xml: " + xmlFields.findFieldWithValue(s).getName() + "  " + s.toString.replace('\n', ' '))

  override def toString() = {
    getClass.getSimpleName() + s"(\n  ${fragmentsToString}\n${xmlsToString})"
  }
}