package org.ensime.util

import java.io.File

trait FileEdit extends Ordered[FileEdit] {
  def file: File
  def text: String
  def from: Int
  def to: Int

  // Required as of Scala 2.11 for reasons unknown - the companion to Ordered
  // should already be in implicit scope
  import scala.math.Ordered.orderingToOrdered

  def compare(that: FileEdit): Int =
    (this.file, this.from, this.to, this.text).compare((that.file, that.from, that.to, that.text))
}

case class TextEdit(file: File, from: Int, to: Int, text: String) extends FileEdit

// the next case classes have weird fields because we need the values in the protocol
case class NewFile(file: File, from: Int, to: Int, text: String) extends FileEdit
object NewFile {
  def apply(file: File, text: String): NewFile = new NewFile(file, 0, text.length - 1, text)
}

case class DeleteFile(file: File, from: Int, to: Int, text: String) extends FileEdit
object DeleteFile {
  def apply(file: File, text: String): DeleteFile = new DeleteFile(file, 0, text.length - 1, text)
}

object FileEdit {

  import scala.tools.refactoring.common.{ TextChange, NewFileChange, Change }

  def fromChange(ch: Change): FileEdit = {
    ch match {
      case ch: TextChange => TextEdit(ch.file.file, ch.from, ch.to, ch.text)
      case ch: NewFileChange => NewFile(ch.file, ch.text)
    }
  }

  def applyEdits(ch: List[TextEdit], source: String): String = {
    (source /: ch.sortBy(-_.to)) { (src, change) =>
      src.substring(0, change.from) + change.text + src.substring(change.to)
    }
  }

}
