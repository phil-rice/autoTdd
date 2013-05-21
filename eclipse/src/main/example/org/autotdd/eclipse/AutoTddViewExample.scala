package org.autotdd.eclipse

import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.SWT
import net.miginfocom.swt.MigLayout

class AutoTddViewExample(parent: Composite, val files: FilesTimesAndContents) extends Composite(parent, SWT.NULL) {
  setLayout(new MigLayout("fill", "[400][grow]", "[grow]"))
  val list = new org.eclipse.swt.widgets.List(this, SWT.WRAP | SWT.READ_ONLY); list.setLayoutData("grow")
  val textArea = new StyledText(this, SWT.WRAP | SWT.READ_ONLY); textArea.setLayoutData("grow")
  files.addListener((event) =>
    event match {
      case InsertedFile(i, fct) =>
        list.add(fct.file.getName, i)
      case DeletedFile(i, fct) =>
        list.remove(i)
        if (i == list.getSelectionIndex()) textArea.setText("")
      case UpdatedFile(i, fct) =>
        if (i == list.getSelectionIndex())
          textArea.setText(fct.content)
    })
}
//
//class Something {
//  def fixture(name: String, code: => Unit)
//}
//class taa extends Composite(null, SWT.NULL) {
//  val list = new org.eclipse.swt.widgets.List(this, SWT.WRAP | SWT.READ_ONLY); list.setLayoutData("grow")
//  val textArea = new StyledText(this, SWT.WRAP | SWT.READ_ONLY); textArea.setLayoutData("grow")
//
//  val engine: Something = null
//  engine.fixture("fixture1", {
//    list.removeAll();
//    list.add("item1");
//    list.add("item2");
//  }
//  //I want to describe a story
//  //The file time/date and content changed
//  //update was called
//  //the files variable was changed
//  //an event was triggered
//  //when the event got to the list, the list was updated
//  
//  //with several stories like this...first everything is well tested, secondly it might be possible to understand what is happening
//  
//  //But I want to read as though things are decoupled...
//  
//  
//  
//  fileAccess = Fixture0
//  files = new Files(fileAccess)
//  when {files.updateFiles}
//     I cause three events: 
//        InsertEvent();
//        InsertEvent();
//        InsertEvent();
//     
// when {InsertEvent} is processed by {list}
//    an item is added to the list
//    
// when all three have been processed the list looks like this
// 
// 
//  
//  
//  
//  
//  
//  
//  
//  
//  engine.listener(files = ?, list = ?, {
//    
//  }
//  
//  
//  )
//
//}