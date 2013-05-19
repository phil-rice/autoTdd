package org.autotdd.eclipse

import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.Status
import org.eclipse.swt.widgets.Display

class Jobs(val display: Display) extends NeedsSwtThread {

  def executeAsJob(calculate: => Unit) = {
    val job = new Job("My Job") {
      def run(monitor: IProgressMonitor): IStatus = {
        calculate
        Status.OK_STATUS;
      }
    };
    job.schedule()
    job
  }

  def executeRepeatadlyAsJob(period: Int, calculate: => Unit) = {
    val job = new Job("My Job") {
      var stop = false
      def run(monitor: IProgressMonitor): IStatus = {
        try {
          calculate
        } finally {
          if (!stop)
            schedule(period)
        }
        Status.OK_STATUS;
      }
    }
    job.schedule()
    job
  }
}