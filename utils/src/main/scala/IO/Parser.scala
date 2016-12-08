package utils.IO

import java.io.File

import scala.io.Source

/**
  * Parses a FILE of format:
  * 00002131 0.1, 0.2, 3.0(x256)
  *
  * @param data a file of the above format
  * @return Parser instance with iterable capabilities parsing data as requested
  */
class Parser(data:File) extends Serializable  {
  // TODO Implement Buffer instead
  private val iterator = Source.fromFile(data.getAbsoluteFile).getLines()

  def hasNext : Boolean = iterator.hasNext

  val size = {
    val iterator = Source.fromFile(data.getAbsoluteFile).getLines()
    iterator.length
  }

  // TODO This can be optimized a LOT!
  def next : (String, Array[Float]) = {
    val l = iterator.next
    val la = l.toString().split(" ")
    val id = la.head
    val vector = l.tail.map(x => x.toFloat).toArray

    (id, vector)
  }
}
