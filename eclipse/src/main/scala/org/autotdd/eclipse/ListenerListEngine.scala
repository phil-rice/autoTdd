package org.autotdd.eclipse

import org.eclipse.swt.widgets.Display
import org.eclipse.swt.SWTException

trait ListenerList {
  type EventFn
  protected var listeners = List[EventFn]()
  def addListener(l: EventFn) = { listeners = l :: listeners }
  protected def forEachListener(callback: (EventFn) => Unit) = {
    for (l <- listeners) {
      try {
        callback(l)
      } catch {
        case t: Throwable => dealWithException(l, t)
      }
    }
    postForEachListener(callback)
  }
  protected def dealWithException(l: EventFn, t: Throwable) = throw t
  protected def postForEachListener(callback: (EventFn) => Unit) = {}

}

class AggregateException(val exceptions: List[Throwable]) extends Exception(exceptions.toString)

trait ListenerListAggregatesException extends ListenerList {
  override protected def dealWithException(l: EventFn, t: Throwable) = throw t
  override protected def postForEachListener(callback: (EventFn) => Unit) = {}
}

trait ListenerList1[Event] extends ListenerList {
  type EventFn = (Event) => Unit
  def fire(e: Event) = forEachListener((fn) => fn(e))
}

trait ListenerList2[Event1, Event2] extends ListenerList {
  type EventFn = (Event1, Event2) => Unit
  def fire(e1: Event1, e2: Event2) = forEachListener((fn) => fn(e1, e2))
}

trait ListenerList3[Event1, Event2, Event3] extends ListenerList {
  type EventFn = (Event1, Event2, Event3) => Unit
  def fire(e1: Event1, e2: Event2, e3: Event3) = forEachListener((fn) => fn(e1, e2, e3))
}

//Testing
//adding listeners causes them to fire
//Exceptions in listeners cause them to be handed to dealwithExceptions
//at end is called after forEachListener even if exceptions have happened
//swt aware: on swt thread
//swt aware: on other thread
