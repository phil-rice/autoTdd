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
    case true => acc \ p.element
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
    for (i <- 1 to l.size)
      yield l.take(i)
  def allPaths(fragments: List[Fragment[_, _, _, _]]) =
    fragments.flatMap((f) => subLists(f.paths.reverse))
  def allButLast(l: List[Path]) = l.take(l.size - 1)
  def apply[Structure, End, Result](fragments: List[Fragment[Structure, End, Result, _]]): PathMap[Structure, End, Result] = {
    val elemToFragment = fragments.groupBy(_.raw)
    val map = elemToFragment.mapValues((f) => {
      val paths = allPaths(f)
      val map = paths.foldLeft(MapOfList[List[Path], List[Path]]())((acc, p) =>
        p.size match {
          case 1 => acc
          case _ => acc.put(allButLast(p), p)
        })
      map
    })
    val roots = elemToFragment.mapValues(_.map(_.paths.reverse).map(_.head).removeDuplicates.toList)
    new PathMap[Structure, End, Result](map, roots, elemToFragment)
  }
}

/** So this is all the ends in a parent / child map. with a list of the roots. */
case class PathMap[Structure, End, Result](val map: Map[Structure, MapOfList[List[Path], List[Path]]], val roots: Map[Structure, List[Path]], val fullPaths: Map[Structure, List[Fragment[Structure, End, Result, _]]]) {
  //  def walkPaths = roots.flatMap((p) => walkPathsSelfAndChildren(List(p)))
  //  private def walkPathsSelfAndChildren(parent: List[Path]): List[List[Path]] = parent :: map(parent).flatMap(walkPathsSelfAndChildren(_))
  def apply(structure: Structure, path: List[Path]) = map(structure)(path)
}

object XmlSituation {

}
trait XmlSituation {
  private def instantiateLazyVals(clazz: Class[_]): Unit = {
    val methods = getClass.getDeclaredMethods().filter((m) => m.getParameterTypes().length == 0 && clazz.isAssignableFrom(m.getReturnType()))
    for (m <- methods) {
      m.setAccessible(true)
      m.invoke(XmlSituation.this)
    }
  }
  private def value[T](f: Field) = {
    f.setAccessible(true);
    val value = f.get(XmlSituation.this).asInstanceOf[T];
    value
  }

  private lazy val instantiateLazyVals: Unit = { instantiateLazyVals(classOf[XmlFragment[_]]); instantiateLazyVals(classOf[Elem]) }

  private lazy val fragmentFields = getClass.getDeclaredFields.filter((f) => classOf[XmlFragment[_]].isAssignableFrom(f.getType())).toList
  private lazy val xmlFields = getClass.getDeclaredFields.filter((f) => classOf[Elem].isAssignableFrom(f.getType())).toList

  lazy val fragmentFieldMap = { instantiateLazyVals; Map[Field, XmlFragment[_]]() ++ fragmentFields.map((f) => (f -> value[XmlFragment[_]](f))) }
  lazy val xmlFieldMap = { instantiateLazyVals; Map[Field, Elem]() ++ xmlFields.map((f) => (f -> value[Elem](f))) }

  private lazy val xmls = xmlFields.map(value[Elem](_))
  private lazy val fragments = fragmentFields.map(fragmentFieldMap)

  lazy val pathMap = PathMap[Elem, NodeSeq, String](fragments)

  protected lazy val fragmentsToString = fragmentFields.map(((f) => {
    val frag = fragmentFieldMap(f)
    val end = frag.end(frag.raw, frag.paths)
    val endString = end.mkString(", ");
    val resultString = try { frag.endToResult(end) } catch { case e: Throwable => e.getClass.getSimpleName() + " " + e.getMessage }
    s"${f.getName} = ${resultString} -- [${endString}]"
  })).mkString("\n  ")

  case class IndentAndString(indent: String, string: String) {
    def indentedBlank = IndentAndString(indent + "  ", "")
  }

  def selfAndChildren(s: Elem): (IndentAndString, List[Path]) => IndentAndString = (acc, p) => {
    val fragments = pathMap.fullPaths(s).filter(_.paths.reverse == p)
    val valueString = fragments.size match {
      case 0 => "";
      case _ => " = " + fragments.map((f) => try { f.endToResult(f.end(s, f.paths)) } catch { case e: Throwable => e.getClass.getSimpleName() + " " + e.getMessage }).mkString(",")
    }
    val myString = acc.indent + p.mkString("") + valueString + "\n"
    val childrensString = pathMap(s, p).foldLeft(acc.indentedBlank)(selfAndChildren(s))
    new IndentAndString(acc.indent, acc.string + myString + childrensString.string)
  }
  protected def findNameFor(x: Elem) = xmlFields.find((f) => xmlFieldMap(f) == x) match {
    case Some(f) => f.getName
    case None => "{Unknown}"
  }
  protected lazy val xmlsToString = pathMap.roots.keys.map((s) => "  Xml: " + findNameFor(s) + "  " + s.toString.replace('\n', ' ') + "\n" + {
    pathMap.roots(s).foldLeft(IndentAndString("    ", ""))((acc, r) => selfAndChildren(s)(acc, List(r)))
  }.string).mkString("\n")

  override def toString() = {
    getClass.getSimpleName() + s"(\n  ${fragmentsToString}\n${xmlsToString})"
  }
}