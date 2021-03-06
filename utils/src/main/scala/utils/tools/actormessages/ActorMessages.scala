package utils.tools.actormessages

import utils.tools.Distance

import scala.collection.mutable.ArrayBuffer
trait ActorMessages {

}

abstract class Status()
case object Ready extends Status
case class InProgress(progress:Int) extends Status
case object NotReady extends Status
// PerformanceTester
case class InitializeStructure(seed:Long)

// LSHStructure
case class InitializeTableHandlers(hf:String, tables:Int, functions:Int, numOfDim:Int, inputFile:String, knn:Int)

// Table handler
case class InitializeTables(hf:String, tables:Int, functions:Int, numOfDim:Int, seed:Long, inputFile:String)

// Table
case class FillTable(buildFromFile:String)



// Statuses
case object IsReady
case object StructureReady
case class TableHandlerStatus(statuses:Array[Status])
case class TableStatus(id:Int, status:Status)
case object GetStatus

// Query
case class Query(q:(Int, Array[Float]), range:Double, probingScheme:String, distanceMeasure:Distance,k:Int, numOfProbes:Int)
case class StructureQuery(q:Array[Float],range:Int)
case class QueryResult(candidates:ArrayBuffer[(Int, Float)], numberOfCandidates:Int) // id, distToQ

// Else
case class RunAccuracyTest(range:Double, k:Int, epochs:Int)
