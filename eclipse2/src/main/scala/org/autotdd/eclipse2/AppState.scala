package org.autotdd.eclipse2

import java.io.File

case class AppState(fileAccess: FileAccess, fileCache: List[FileContentAndTime])

object AppState {
  val fileAccessL = Lens[AppState, FileAccess](_.fileAccess, (as, fa) => as.copy(fileAccess = fa));
  val fileCacheL = new Lens[AppState, List[FileContentAndTime]](_.fileCache, (as, fc) => as.copy(fileCache = fc.sortBy((fct) => fct.file.getName))) {
    def indexOf(as: AppState, f: File): Int = get(as).indexWhere(_.file == f)
    def add(as: AppState, fct: FileContentAndTime) = {
      val index = indexOf(as, fct.file);
      if (index == -1)
        set(as, fct :: get(as))
      else
        set(as, get(as).patch(index, List(fct), 1))
    }
    def apply(as: AppState, f: File) = get(as).find(_.file == f)
  }
}
