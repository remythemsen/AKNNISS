package IO
import java.io.File

import scala.collection.immutable.Stream.cons
import scala.io.Source

/**
  * Created by remeeh on 9/26/16.
  */
object Parser {
  def parseInput(data:File) : Stream[(String, Vector[Double])] = {
    // TODO: Just handle the file directly
    // TODO: Optimize for speed
    convertToTuples(Source.fromFile(data.getAbsolutePath).getLines())
  }
  // TODO Make sure its tail recursive
  def convertToTuples(data: Iterator[String]) : Stream[(String, Vector[Double])] = {
    if(data.hasNext) {
      val set = data.take(2).toList
      cons(Tuple2(set(0).toString.substring(49), set(1).toString.split(" ").map(x => x.toDouble).toVector), convertToTuples(data))
    } else {
      Stream.Empty
    }
  }
}
