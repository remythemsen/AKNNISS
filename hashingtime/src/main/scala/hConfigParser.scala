import utils.tools.{Cosine, Euclidean}

import scala.io.Source

/**
  * Created by remeeh on 12/19/16.
  */
class hConfigParser(htconfig:String) {
  val file:Iterator[String] = Source.fromFile(htconfig).getLines

  def hasNext:Boolean = file.hasNext
  def next:HashingConfig = {
    val config = file.next.toString.split(" ")
    HashingConfig(
      config(0).toInt, // N
      config(1).toInt, // m
      config(2), // hashFunction
      config(3).toInt, //d
      config(4), //datafile
      config(5) //warmupfile
    )
  }
}
