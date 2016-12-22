package IO

import java.io.File

import scala.collection.parallel.mutable.ParArray
import scala.io.Source

/**
  * Parses a FILE of format:
  * ############ (x49) 00002131
  * 0.1, 0.2, 3.0(x4096)
  *
  * @return Parser instance with iterable capabilities parsing data as requested
  */
class DisaFileParser(file:File) {

  private val iterator = Source.fromFile(file,15000).getLines()

  def hasNext : Boolean = iterator.hasNext

  val size = {
    val iterator = Source.fromFile(file).getLines()
    iterator.length/2
  }

  def next : (Int, Array[Float]) = {
    // Get id line
    val id = iterator.next.substring(49).toInt
    // Get Vector line, and convert to float
    val vecLine = iterator.next.split(" ")
    val vector = for {
      component <- vecLine
    } yield component.toFloat

    (id, vector)
  }
}
