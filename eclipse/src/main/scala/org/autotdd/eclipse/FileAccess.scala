package org.autotdd.eclipse

import java.io.File
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
//last modified is awkward, so I'll probably just rely on inspection.

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

trait FilesTimesAndContents extends FileAccess with ListenerList1[FileEvent] {
  private var files = List[FileContentAndTime]()
  private def findIndex(f: File) = files.indexWhere((ct) => ct.file == f)

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
