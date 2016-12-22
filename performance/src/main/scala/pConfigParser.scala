import java.io.File

import LSH.hashFunctions.{CrossPolytope, Hyperplane}
import utils.tools.{Cosine, Euclidean}

import scala.io.Source

/**
  * Created by remeeh on 12/19/16.
  */
class pConfigParser(pfconfig:String) {
  val file:Iterator[String] = Source.fromFile(pfconfig).getLines

  def hasNext:Boolean = file.hasNext
  def next:PerformanceConfig = {
    val config = file.next.toString.split(" ")
    PerformanceConfig(
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
      config(12).toInt, // number of probes
      config(13) // knn structure location
    )
  }
}
