package IO
import java.io.File
import java.io.FileInputStream
import java.nio.Buffer
import java.nio.channels.FileChannel.MapMode._

import scala.annotation.tailrec
import scala.collection.immutable.Stream.cons
import scala.io.Source

/**
  * Parses a FILE of format:
  * ##############################(x49)Id
  * 0.1, 0.2, 3.0(x4096)
  *
  * @param data a file of the above format
  * @return Parser instance with iterable capabilities parsing data as requested
  */
@SerialVersionUID(100L)
class Parser(data:File) extends Serializable  {
  // TODO Implement Buffer instead
  //val fileSize = data.length
  //val stream = new FileInputStream(data)
  //val buffer = stream.getChannel.map(READ_ONLY, 0, fileSize)

  private val iterator = Source.fromFile(data.getAbsoluteFile).getLines()

  // The approximate size of data
  val size = Source.fromFile(data.getAbsoluteFile).getLines().size / 2
  val vLength = {
    val xiterator = Source.fromFile(data.getAbsoluteFile).getLines()
    val set = xiterator.take(2).toList
    set(1).toString.split(" ").length
  }

  def hasNext : Boolean = iterator.hasNext

  def next : (String, Vector[Double]) = {
    val set = iterator.take(2).toList
    (set(0).toString.substring(49), set(1).toString.split(" ").map(x=>x.toDouble).toVector)
  }
}
