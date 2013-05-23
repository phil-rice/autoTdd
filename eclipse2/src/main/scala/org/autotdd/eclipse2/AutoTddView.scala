package org.autotdd.eclipse2

import java.io.File
import scala.collection.JavaConversions._
import scala.io.Source
import org.autotdd.engine.Engine1
import org.autotdd.engine.tests._
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.events.SelectionListener
import org.eclipse.swt.widgets.Composite
import org.eclipse.ui.part.ViewPart
import org.junit.runner.RunWith
import net.miginfocom.swt.MigLayout
import org.autotdd.engine.Engine3
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.Display

object AppStateFixture {
  val f1 = new File("1");
  val f2 = new File("2");
  val f3 = new File("3");

  val fct1 = FileContentAndTime(f1, "contentf1", 1)
  val fct1b = FileContentAndTime(f1, "contentf1", 10)
  val fct2 = FileContentAndTime(f2, "contentf2", 1)
  val fct3 = FileContentAndTime(f3, "contentf3", 1)

  val fa1 = new MockFileAccess(fct1, fct2, fct3)
  val fa1b = new MockFileAccess(fct1b, fct2, fct3)

  val as_empty_cache = AppState(fa1, List())
  val as_fct12 = AppState(fa1, List(fct1, fct2))
  val as_fct12_f1_changed_on_file_system = AppState(fa1b, List(fct1, fct2))
}

trait UpdateFiles {
  import AppStateFixture._
  import AppState._

  type Because = (File, AutoTddComposite, AppState) => Boolean
  type Do = (File, AutoTddComposite, AppState) => AppState

  val doNothing: Do = (f, ac, as) => as

  val becauseFileIsntInCache: Because = (f, ac, as) => fileCacheL(as, f).isEmpty
  val becauseFileHasChangedOnFileSystem: Because = (f, ac, as) => fileCacheL(as, f).collect { case fct => fct.time != fileAccessL(as)(f).time }.getOrElse(false);
  val becauseFileIsTheSelectedFile: Because = (f, ac, as) => fileCacheL.indexOf(as, f) == ac.currentSelection

  def and(b1: Because, b2: Because): Because = (f, ac, as) => b1(f, ac, as) & b2(f, ac, as)

  val updateGuiAndCacheWhenFileChanges = Engine3[File, AutoTddComposite, AppState, AppState](doNothing) //default is do nothing

  def makeComposite: AutoTddComposite
  val composite = makeComposite
  composite.reset
  updateGuiAndCacheWhenFileChanges.constraint(f1, composite, as_empty_cache,
    /*expected=*/ fileCacheL.set(as_empty_cache, List(fct1)),
    /* code= */ (f, ac, as) => {
      val fct = fileAccessL(as)(f)
      val result = fileCacheL.add(as, fct)
      val index = fileCacheL.indexOf(result, f)
      ac.insert(index, fct)
      result
    }, becauseFileIsntInCache);

  composite.reset
  composite.insert(0, fct1)
  composite.insert(1, fct2)
  updateGuiAndCacheWhenFileChanges.constraint(f1, composite, as_fct12_f1_changed_on_file_system,
    /*expected=*/ fileCacheL.set(as_fct12_f1_changed_on_file_system, List(fct1b, fct2)),
    /* code = */ (f, ac, as) => {
      val fct = fileAccessL(as)(f)
      val index = fileCacheL.indexOf(as, f)
      fileCacheL.add(as, fct)
    }, becauseFileHasChangedOnFileSystem)

  composite.reset
  composite.insert(0, fct1)
  composite.insert(1, fct2)
  composite.setSelection(0)
  updateGuiAndCacheWhenFileChanges.constraint(f1, composite, as_fct12_f1_changed_on_file_system,
    /*expected=*/ fileCacheL.set(as_fct12_f1_changed_on_file_system, List(fct1b, fct2)),
    /* code = */ (f, ac, as) => {
      val fct = fileAccessL(as)(f)
      val index = fileCacheL.indexOf(as, f)
      ac.setText(fct.content)
      fileCacheL.add(as, fct)
    }, and(becauseFileHasChangedOnFileSystem, becauseFileIsTheSelectedFile))

  composite.dispose()
}

class AutoTddComposite(parent: Composite) extends Composite(parent, SWT.NULL) {

  setLayout(new MigLayout("fill", "[400][grow]", "[grow]"))
  val list = new org.eclipse.swt.widgets.List(this, SWT.WRAP | SWT.READ_ONLY); list.setLayoutData("grow")
  val textArea = new StyledText(this, SWT.WRAP | SWT.READ_ONLY); textArea.setLayoutData("grow")
  def insert(index: Int, fct: FileContentAndTime) = list.add(fct.file.getName, index)
  def setSelection(index: Int) = list.setSelection(index)
  def currentSelection: Int = list.getSelectionIndex()
  def reset = list.removeAll()
  def setText(s: String) = textArea.setText(s)
}

object AutoTddComposite {
  def apply(fileAccess: FileAccess, updateFiles: UpdateFiles, parent: Composite) = {
    def directory = AutoTddRunner.directory
    var state = new AppState(fileAccess, List())
    def makeComposite = new AutoTddComposite(Display.getDefault().getActiveShell())
    val composite: AutoTddComposite = new AutoTddComposite(parent) {
      val job = new Jobs(getDisplay()).executeRepeatadlyAsJob(2000, fileAccess.listFiles.foreach(f => state = updateFiles.updateGuiAndCacheWhenFileChanges(f, this, state)));
      override def dispose = {
        job.stop = true;
        job.cancel()
        super.dispose
      }
      list.addSelectionListener(new SelectionListener() {
        override def widgetDefaultSelected(e: SelectionEvent) = {}
        override def widgetSelected(e: SelectionEvent) = {
          list.getSelectionIndex() match {
            case -1 => textArea.setText("");
            case i => textArea.setText(state.fileCache(i).content)
          }
        }

      })
    }
    composite
  }
}
//tests: layout list on left, text area on right. Both filling the space. 
//  when I resize the list and text area should still fill the space
//when the files are updated, then the list model is recreated. Don't care about selection
//when the files are updated the text area receives suitable text. Don't care about selection
//when I click on the list, the text area is populated
//when I start the first item is selected and the text area is populated, if one exists
//If the directory doesn't exist shows <No Engines> in List

//class AutoTddView extends ViewPart  {
//  def directory = AutoTddRunner.directory
//  var state = new AppState(this, List())
//  def makeComposite = new AutoTddComposite(Display.getDefault().getActiveShell())
//  def createPartControl(parent: Composite) = {
//    val composite: AutoTddComposite = new AutoTddComposite(parent) {
//      val job = new Jobs(getDisplay()).executeRepeatadlyAsJob(2000, listFiles.foreach(f => state = updateGuiAndCacheWhenFileChanges(f, this, state)));
//      override def dispose = {
//        job.stop = true;
//        job.cancel()
//        super.dispose
//      }
//      list.addSelectionListener(new SelectionListener() {
//        override def widgetDefaultSelected(e: SelectionEvent) = {}
//        override def widgetSelected(e: SelectionEvent) = {
//          list.getSelectionIndex() match {
//            case -1 => textArea.setText("");
//            case i => textArea.setText(state.fileCache(i).content)
//          }
//        }
//      });
//
//    }
//  }
//  def setFocus() = {}
//}