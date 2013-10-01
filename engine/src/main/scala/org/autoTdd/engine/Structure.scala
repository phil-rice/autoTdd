package org.autotdd.engine

import scala.concurrent.stm._
import java.lang.reflect.Field

trait Fragment[S, End, Result, T] {
  type RealFragment <: Fragment[S, End, Result, T]

  def raw: S
  def endToResult: (End) => Result
  def fn: (End, Result) => Option[T]
  def default: Option[T]
  def paths: List[Path]

  val value = Ref[Option[T]](None)
  val evaluated = Ref[Boolean](false)

  def end(e: S, paths: List[Path]): End
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
        val t = fn(e, r)
        value.set(t)
      }
      value.get
    }
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
case class PathMap[S, End, Result](val map: Map[S, MapOfList[List[Path], List[Path]]], val roots: Map[S, List[Path]], val fullPaths: Map[S, List[Fragment[S, End, Result, _]]]) {
  //  def walkPaths = roots.flatMap((p) => walkPathsSelfAndChildren(List(p)))
  //  private def walkPathsSelfAndChildren(parent: List[Path]): List[List[Path]] = parent :: map(parent).flatMap(walkPathsSelfAndChildren(_))
  def apply(structure: S, path: List[Path]) = map(structure)(path)
}

case class Path(linked: Boolean, element: String) {
  override def toString() = (if (linked) """\""" else """\\""") + element
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
case class IndentAndString(indent: String, string: String) {
  def indentedBlank = IndentAndString(indent + "  ", "")
}

class FieldSet[T](instance: Any, clazz: Class[T]) {

  private def instantiateLazyVals(clazz: Class[_]): Unit = {
    val methods = instance.getClass.getDeclaredMethods().filter((m) => m.getParameterTypes().length == 0 && clazz.isAssignableFrom(m.getReturnType()))
    for (m <- methods) {
      m.setAccessible(true)
      m.invoke(instance)
    }
  }

  private def value(f: Field) = {
    f.setAccessible(true);
    val value = f.get(instance).asInstanceOf[T];
    value
  }
  private lazy val instantiateLazyVals: Unit = instantiateLazyVals(clazz)

  lazy val fields = instance.getClass.getDeclaredFields.filter((f) => clazz.isAssignableFrom(f.getType())).toList
  lazy val fieldMap = { instantiateLazyVals; Map[Field, T]() ++ fields.map((f) => (f -> value(f))) }
  lazy val values = fields.map(fieldMap)
  def findFieldWithValue(x: Any) = fields.find((f) => fieldMap(f) == x).get

}

trait Structure[S, End, Result] {

  protected def findFragmentsToString(fragmentFieldMap: Map[Field, Fragment[S, End, _, _]], endToString: (End) => String) = fragmentFieldMap.keys.map(((f) => {
    val frag = fragmentFieldMap(f)
    val end = frag.end(frag.raw, frag.paths)
    val endString = endToString(end)
    val resultString = try { frag.endToResult(end) } catch { case e: Throwable => e.getClass.getSimpleName() + " " + e.getMessage }
    s"${f.getName} = ${resultString} -- [${endString}]"
  })).mkString("\n  ")

  protected def selfAndChildren(s: S, pathMap: PathMap[S, End, Result]): (IndentAndString, List[Path]) => IndentAndString = (acc, p) => {
    val fragments = pathMap.fullPaths(s).filter(_.paths.reverse == p)
    val valueString = fragments.size match {
      case 0 => "";
      case _ => " = " + fragments.map((f) => try { f.endToResult(f.end(s, f.paths)) } catch { case e: Throwable => e.getClass.getSimpleName() + " " + e.getMessage }).mkString(",")
    }
    val myString = acc.indent + p.mkString("") + valueString + "\n"
    val childrensString = pathMap(s, p).foldLeft(acc.indentedBlank)(selfAndChildren(s, pathMap))
    new IndentAndString(acc.indent, acc.string + myString + childrensString.string)
  }

  protected def structuresToString(pathMap: PathMap[S, End, Result], structureTitle: (S) => String) = pathMap.roots.keys.map((s) => structureTitle(s) + "\n" + {
    pathMap.roots(s).foldLeft(IndentAndString("    ", ""))((acc, r) => selfAndChildren(s, pathMap)(acc, List(r)))
  }.string).mkString("\n")
}
