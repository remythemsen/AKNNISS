import java.io.File

import LSH.hashFunctions.{CrossPolytope, Hyperplane}
import speedtest.SpeedConfig
import utils.tools.{Cosine, Euclidean}

import scala.io.Source

/**
  * Created by chm on 12/20/16.
  */
class sConfigParser(speedconfig:String) {
  val file = Source.fromFile(speedconfig).getLines
  def hasNext = file.hasNext
  def next:SpeedConfig = {
    val config = file.next.toString.split(" ")
    SpeedConfig(
      config(0).toInt, // N
      config(1).toInt, // |qs|
      config(2).toInt, // m
      config(3).toInt,    // |knn|
      config(4).toInt, // L
      config(5).toDouble, // range
      config(6), // qs file
      config(7) match {
        case "Cosine" => Cosine
        case "Euclidean" => Euclidean
      }, // measure
      config(8), // hashFunction
      config(9).toInt, // dimensions
      config(10), // dataSet file
      config(11), // probing scheme
      config(12)//warm up query file
    )
  }
}