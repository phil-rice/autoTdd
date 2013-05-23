package org.autotdd.eclipse

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
case class EngineDescription(val name: String, val description: String)

object AutoTddParser {
  val itemParse = Engine1((item: String) => {
    val index = item.indexOf("\n")
    index match {
      case -1 => throw new IllegalArgumentException(item);
      case _ => EngineDescription(item.substring(0, index), item.substring(index + 1))
    }
  })
  //  itemParse.constraint("abc\ndef", EngineDescription("abc", "def"))
  //  //TODO Need to be able to assert throws exceptions
  //
  //  val parse = Engine1((t: String) => t.split(AutoTddRunner.separator).map(itemParse))
  //  parse.constraint("abc\ndef" + AutoTddRunner.separator + "123\n234",
  //    Array(EngineDescription("abc", "def"), EngineDescription("123", "234")))

  def apply(s: String) = itemParse(s)
}

class AutoTddComposite(parent: Composite, val files: FilesTimesAndContents) extends Composite(parent, SWT.NULL) {

  setLayout(new MigLayout("fill", "[400][grow]", "[grow]"))
  val list = new org.eclipse.swt.widgets.List(this, SWT.WRAP | SWT.READ_ONLY); list.setLayoutData("grow")
  val textArea = new StyledText(this, SWT.WRAP | SWT.READ_ONLY); textArea.setLayoutData("grow")
  files.addListener((event) =>
    event match {
      case InsertedFile(i, fct) =>
        list.add(fct.file.getName, i)
      case DeletedFile(i, fct) =>
        list.remove(i)
        if (i == list.getSelectionIndex())
          textArea.setText("")
      case UpdatedFile(i, fct) =>
        if (i == list.getSelectionIndex())
          textArea.setText(fct.content)
    })
  list.addSelectionListener(new SelectionListener() {
    override def widgetDefaultSelected(e: SelectionEvent) = {}
    override def widgetSelected(e: SelectionEvent) = {
      list.getSelectionIndex() match {
        case -1 => textArea.setText("");
        case i => textArea.setText(files(i))
      }
    }
  });
}

//tests: layout list on left, text area on right. Both filling the space. 
//  when I resize the list and text area should still fill the space
//when the files are updated, then the list model is recreated. Don't care about selection
//when the files are updated the text area receives suitable text. Don't care about selection
//when I click on the list, the text area is populated
//when I start the first item is selected and the text area is populated, if one exists
//If the directory doesn't exist shows <No Engines> in List

class AutoTddView extends ViewPart {
  def createPartControl(parent: Composite) = {
    new AutoTddComposite(parent, new FilesTimesAndContents with FileSystemFileAccess with SwtAwareListenerList {
      val display = parent.getDisplay
      val userHome = System.getProperty("user.home");
      val directory = new File(userHome, ".autoTdd")
    }) {
      val job = new Jobs(getDisplay).executeRepeatadlyAsJob(2000, { files.updateFiles });
      override def dispose = {
        job.stop = true;
        job.cancel()
        super.dispose
      }

    }
  }
  def setFocus() = {}
}