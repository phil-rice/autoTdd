package org.autotdd.carers

import scala.xml.NodeSeq
import java.lang.reflect.Field
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
  def paths: List[Path]

  val value = Ref[Option[T]](None)
  val evaluated = Ref[Boolean](false)

  def end(e: Structure, paths: List[Path]): End
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
        val e = end(raw, paths);
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
  def end(e: Elem, paths: List[Path]): NodeSeq = paths.reverse.foldLeft(e.asInstanceOf[NodeSeq])((acc, p) => p.linked match {
    case true =>
      val r = acc \ p.element
      val t = r.text
      r
    case false => acc \\ p.element
  })
}

case class MapOfList[K, V](val map: Map[K, List[V]] = Map[K, List[V]]()) {

  val emptyList = List[V]()
  def apply(k: K) = map.getOrElse(k, emptyList)
  def put(k: K, v: V) =
    MapOfList(map + (k -> (map.get(k) match {
      case Some(oldList) => v :: oldList
      case None => List(v)
    })))
}

object PathMap {
  def subLists[X](l: List[X]) =
    for (i <- 1 to l.size - 1)
      yield l.take(i)
  def allPaths(fragments: List[Fragment[_, _, _, _]]) =
    fragments.flatMap((f) =>
      subLists(f.paths))
  def allButLast(l: List[Path]) = l.take(l.size - 1)
  def apply(fragments: List[Fragment[_, _, _, _]]): PathMap = {
    val paths = allPaths(fragments)
    paths.foldLeft(new PathMap(fullPaths = fragments.map(_.paths.reverse)))((acc, p) =>
      p.size match {
        case 1 => new PathMap(acc.map, p.head :: acc.roots, acc.fullPaths)
        case 2 => new PathMap(acc.map.put(allButLast(p), p), acc.roots, acc.fullPaths)
      })
  }
}
/** So this is all the ends in a parent / child map. with a list of the roots. */
case class PathMap(val map: MapOfList[List[Path], List[Path]] = MapOfList[List[Path], List[Path]](), val roots: List[Path] = List(), val fullPaths: List[List[Path]] = List[List[Path]]()) {
  def walkPaths = roots.flatMap((p) => walkPathsSelfAndChildren(List(p)))
  private def walkPathsSelfAndChildren(parent: List[Path]): List[List[Path]] = parent :: map(parent).flatMap(walkPathsSelfAndChildren(_))
}

object XmlSituation {

}
trait XmlSituation {
  lazy val instantiateLazyVals: Unit = {
    val methods = getClass.getDeclaredMethods().filter((m) => m.getParameterTypes().length == 0 && classOf[XmlFragment[_]].isAssignableFrom(m.getReturnType()))
    for (m <- methods) {
      m.setAccessible(true)
      m.invoke(XmlSituation.this)
    }

  }
  lazy val fields = getClass.getDeclaredFields.filter((f) => classOf[XmlFragment[_]].isAssignableFrom(f.getType())).toList
  lazy val fieldMap = {
    instantiateLazyVals; Map[Field, XmlFragment[_]]() ++ fields.map((f) => {
      f.setAccessible(true);
      val frag = f.get(XmlSituation.this).asInstanceOf[XmlFragment[_]];
      (f -> frag)
    })
  }

  lazy val fragments = fields.map(fieldMap)
  lazy val pathMap = PathMap(fragments)
  lazy val fragmentsToString = fields.map(((f) => {
    val frag = fieldMap(f)
    val end = frag.endToResult(frag.end(frag.raw, frag.paths))
    s"${f.getName} = [${end}]"
  })).mkString("\n  ")
  override def toString() = {
    getClass.getSimpleName() + s"(\n  ${fragmentsToString}\n)"
  }
}