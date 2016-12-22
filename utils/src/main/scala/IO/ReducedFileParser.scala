package utils.IO
import java.io.File

import scala.io.Source

/**
  * Parses a FILE of format:
  * 00002131 0.1, 0.2, 3.0(x256)
  *
  * @return Parser instance with iterable capabilities parsing data as requested
  */
class ReducedFileParser(file:File) {

  private val iterator = Source.fromFile(file,5000).getLines()

  def hasNext : Boolean = iterator.hasNext

  val size = {
    val iterator = Source.fromFile(file).getLines()
    iterator.length
  }

  def next : (Int, Array[Float]) = {
    val l = iterator.next
    val la = l.split(" ")
    val id = la.head.toInt
    val vector = for {
      component <- la.tail
    } yield component.toFloat

    (id, vector)
  }
}
