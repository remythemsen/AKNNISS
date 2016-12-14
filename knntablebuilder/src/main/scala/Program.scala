import java.io.{File, FileOutputStream, ObjectOutputStream}

import utils.IO.ReducedFileParser
import utils.tools.{Cosine, Distance}

import scala.collection.mutable

case class Config(buildFromFile:String, queries:String, outPath:String, n:Int, qn:Int, knn:Int, measure:Distance)

object Program extends App {

  val config = new Config(
      "./knntablebuilder/data/descriptors-decaf-random-sample-reduced.data", // Data
      "./knntablebuilder/data/query-261.data",  // Q File
      "./knntablebuilder/data",                   // Out path
      39286,                      // N
      161,                        // Queries
      30,                         // KNN
      Cosine)                     // MEASURE

  val data = new ReducedFileParser(new File(config.buildFromFile))
  val queries = new ReducedFileParser(new File(config.queries))
  val structure = new mutable.HashMap[Int, Array[(Int, Float)]]

  println("Building Structure")
  var progress = 0.0
  var percentile = config.qn / 100

  while(queries.hasNext) {
    var q = queries.next
    var knnFinder = new KNN


    structure += (q._1 -> knnFinder.findKNearest(q, config.knn, config.buildFromFile, config.measure))
    progress+=1

    if(progress % percentile == 0) {
      println(((progress / config.qn) * 100).toInt.toString + "%")
    }
  }

  println("Saving structure to disk...")
  val oos = new ObjectOutputStream(new FileOutputStream(config.outPath+"/knnstructure"))
  oos.writeObject(structure)
  oos.close
  println("structure was saved..")
}
