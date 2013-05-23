package org.autotdd.eclipse2

import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.Display
import java.io.File

object AutoTddViewRunner {

  def main(args: Array[String]) {
    val display = new Display
    val shell = new Shell(display)
    val composite = AutoTddComposite(new FileSystemFileAccess() {
      val userHome = System.getProperty("user.home");
      val directory = new File(userHome, ".autoTdd")
    }, new UpdateFiles() {
      def makeComposite = new AutoTddComposite(shell)
    }, shell);
    composite.pack
    shell.pack();
    shell.open();
    while (!shell.isDisposed())
      if (!display.readAndDispatch)
        display.sleep()
  }
}