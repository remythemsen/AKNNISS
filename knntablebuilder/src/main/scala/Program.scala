import java.io.{File, FileOutputStream, ObjectOutputStream}
import java.util.concurrent.Executors

import scala.concurrent.duration._
import utils.IO.ReducedFileParser
import utils.tools.{Cosine, Distance}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Await, ExecutionContext, Future}

case class Config(buildFromFile:String, queries:String, outPath:String, n:Int, qn:Int, knn:Int, distance:Distance)

object Program extends App {

  // LoadKNN.concatKNN()

  implicit val ec = ExecutionContext.fromExecutorService(Executors.newWorkStealingPool(12))

  val config = new Config(
      "data/descriptors-decaf-1m.data", // Data
      "data/queries-5-2346.data", //queries-5-8069.data",       // Q File
      "data",                     // Out path
      1008935,//20172529,                      // N
      2346,     //8063                   // Queries
      30,                         // KNN
      Cosine)                     // MEASURE

  val data = new ReducedFileParser(new File(config.buildFromFile))
  val queries = new ReducedFileParser(new File(config.queries))
  //val structure = new mutable.HashMap[Int, mutable.PriorityQueue[(Int, Float)]]
  val structure=new mutable.HashMap[Int,Array[(Int,Float)]]

  println(queries.size)
  println("Building Structure")
  var progress = 0.0
  var percentile = config.n / 100

  var priorityQueues = new Array[mutable.PriorityQueue[(Int, Float)]](config.qn)
  for(p <- 0 until config.qn) {
    priorityQueues(p) = new mutable.PriorityQueue[(Int, Float)]
  }

  var loadedQueries = new Array[(Int, Array[Float])](config.qn)
  for(i <- 0 until loadedQueries.length) {
    loadedQueries(i) = queries.next
  }

  while(data.hasNext) {
    var futures = new ArrayBuffer[Future[Unit]]
    var dataPoint = data.next
    for(i <- 0 until loadedQueries.length) {
      var q = loadedQueries(i)
      futures += Future {
        val pqEntry = (q._1, config.distance.measure(dataPoint._2, q._2))
        if(priorityQueues(i).size <= 30) {
          priorityQueues(i).enqueue(pqEntry)
        }
        else {
          if(priorityQueues(i).head._2 > pqEntry._2) {
            priorityQueues(i).dequeue()
            priorityQueues(i).enqueue(pqEntry)
          }
        }
      }
    }
    Await.result(Future.sequence(futures), 20 seconds)
    progress+=1


    if(progress % percentile == 0) {
      println(((progress / config.n) * 100).toInt + "%")
    }
  }

//  for(i<-0 until loadedQueries.size) {
//    structure += ((loadedQueries(i)._1, priorityQueues(i)))
//  }

  //println(priorityQueues.size+"------"+loadedQueries.size)

  for(i<-0 until priorityQueues.size) {
    val arrayTuple = new Array[(Int, Float)](priorityQueues(i).size)
    for (j <- 0 until priorityQueues(i).size) {
      arrayTuple(j) = (priorityQueues(i).dequeue())
    }
    structure+=((loadedQueries(i)._1,arrayTuple))
  }

  println("Saving structure to disk...")
  val oos = new ObjectOutputStream(new FileOutputStream(config.outPath+"/knnstructure"))
  oos.writeObject(structure)
  oos.close
  println("structure was saved..")
}
