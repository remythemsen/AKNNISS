package IO
import java.io.File
import java.io.FileInputStream
import java.nio.Buffer
import java.nio.channels.FileChannel.MapMode._

import scala.annotation.tailrec
import scala.collection.immutable.Stream.cons
import scala.io.Source

/**
  * Created by remeeh on 9/26/16.
  */
object Parser {
  /**
    * Parses a FILE of format:
    * ##############################(x49)Id
    * 0.1, 0.2, 3.0(x4096)
    *
    * @param data a file of the above format
    * @return return a tuple with data size, and a stream of tuples containing datapoint representations
    */
  def parseInput(data:File) : (Long, Stream[(String, Vector[Double])]) = {
    // TODO: Just handle the file directly
    // TODO: Optimize for speed
    val fileSize = data.length
    val stream = new FileInputStream(data)
    val buffer = stream.getChannel.map(READ_ONLY, 0, fileSize)

    //val iterator = Source.fromFile(data.getAbsoluteFile).getLines()
    val res = convertToTuples(buffer, Stream(), 0)

    (39290, res)
  }

  @tailrec def convertToTuples(data: Buffer[String], res: Stream[(String, Vector[Double])], i:Int) : Stream[(String, Vector[Double])] = {
    data.
    if(!data.hasNext) {
      res
    } else {
      println(i)
      val set = data.take(2).toList
      val str = cons(Tuple2(set(0).toString.substring(49), set(1).toString.split(" ").map(x => x.toDouble).toVector), res)
      convertToTuples(data, str, i+1)
    }
  }
}
