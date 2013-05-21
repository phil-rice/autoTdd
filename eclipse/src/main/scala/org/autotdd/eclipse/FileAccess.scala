package org.autotdd.eclipse

import java.io.File
import org.autotdd.engine._

import java.util.ArrayList

import scala.io.Source

case class FileContentAndTime(val file: File, val content: String, val time: Long)

trait FileAccess {
  def directory: File
  def contents(f: File): String
  def exists(f: File): Boolean
  def lastModified(f: File): Long
  def listFiles: Array[File]
}

trait FileSystemFileAccess extends FileAccess {
  def contents(f: File): String = Source.fromFile(f).mkString
  def lastModified(f: File): Long = f lastModified
  def listFiles = directory.listFiles.filter(_.getName.endsWith(".attd"))
  def exists(f: File) = f exists
}

//check listFiles returns the list of files in the directory
//check that it returns  empty list if the directory doesn't exist, and that it doesn't create the directory
//check contents returns the contents of the file
//last modified returns the last modified

class MockFileAccess(contentMap: Map[File, String], lastModifiedMap: Map[File, Long]) extends FileAccess {
  def directory: File = null
  def contents(f: File) = contentMap(f)
  def lastModified(f: File) = lastModifiedMap(f)
  def listFiles = contentMap.keys.toArray
  def exists(f: File) = contentMap contains f
}

sealed abstract class FileEvent
case class InsertedFile(val index: Int, val fct: FileContentAndTime) extends FileEvent
case class DeletedFile(val index: Int, val fct: FileContentAndTime) extends FileEvent
case class UpdatedFile(val index: Int, val fct: FileContentAndTime) extends FileEvent

trait FilesForTesting {
  val f1 = new File("1");
  val f2 = new File("2");
  val f3 = new File("3");
  val fct1 = FileContentAndTime(f1, "contentf1", 1)
  val fct1b = FileContentAndTime(f1, "contentf1", 10)
  val fct2 = FileContentAndTime(f2, "contentf2", 1)
  val fct3 = FileContentAndTime(f3, "contentf3", 1)
}

trait FilesTimesAndContents extends FileAccess with ListenerList1[FileEvent] with FilesForTesting {
  private var files = List[FileContentAndTime]()
  private def findIndex(f: File) = files.indexWhere((ct) => ct.file == f)

  val updateFileEngine = Engine2[File, List[FileContentAndTime], (Option[FileEvent], List[FileContentAndTime])]((f, l) => (None, l));
  updateFileEngine.constraint(f1, List(), (Some(InsertedFile(0, fct1)), List(fct1)),
    (f, l) => {
      val fct = FileContentAndTime(f, contents(f), lastModified(f));
      val newL = (fct :: l) sortBy ((f) => f.file.getName);
      val index = newL.indexOf(fct)
      (Some(InsertedFile(index, fct)), newL)
    },
    (f, l) => l.find((fct) => fct.file == f).isEmpty);
  updateFileEngine.constraint(f2, List(fct1), (Some(InsertedFile(1, fct2)), List(fct1, fct2)))
  updateFileEngine.constraint(f1, List(fct2, fct3), (Some(InsertedFile(0, fct1)), List(fct1, fct2, fct3)))
  
  val update2 = EngineListenerList2[File, List[FileContentAndTime], List[FileContentAndTime]]((f, l)=> l)
  update2.constraintWithEvent(f1, List(), List(InsertedFile(0, fct1)), 
    (f, l) => (FileContentAndTime(f, contents(f), lastModified(f)) :: l) sortBy ((f) => f.file.getName),
    (f, l) => l.find((fct) => fct.file == f).isEmpty),
    ((f, l, newL) => 
      val index = newL.indexWhere((fct)=>fct.file==f); 
      InsertedFile(index, newL(index)));
  
  

  def updateFiles {
    listFiles.foreach((f: File) => {
      findIndex(f) match {
        case -1 =>
          val newFct = FileContentAndTime(f, contents(f), lastModified(f))
          files = (newFct :: files).sortBy((fct) => fct.file.getName)
          fire(InsertedFile(findIndex(f), newFct))
        case n =>
          exists(f) match {
            case true =>
              val newFct = FileContentAndTime(f, contents(f), lastModified(f))
              files = files.patch(n, List(newFct), 1)
              fire(UpdatedFile(findIndex(f), newFct))
            case false =>
              fire(DeletedFile(n, files(n)))
              files = files.filterNot(_.file == f)
          }
      }
    })
    files
  }

  def apply(f: File): String = contents(f)
  def apply(i: Int): String = files(i).content

}

 
//Test first time updateFiles is called listeners are called
//Test listeners are only called by updateFiles after this if the lastmodified has changed
//files doesn't get bigger across time.
