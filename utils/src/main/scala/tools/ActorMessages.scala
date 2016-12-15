package utils.tools.actorMessages

import tools.status.Status

import scala.collection.mutable.ArrayBuffer
trait ActorMessages {

}

// PerformanceTester
case class InitializeStructure(seed:Long)

// LSHStructure
case class InitializeTableHandlers(hf:String, tables:Int, functions:Int, numOfDim:Int, seed:Long, inputFile:String)

// Table handler
case class InitializeTables(hf:String, tables:Int, functions:Int, numOfDim:Int, seed:Long, inputFile:String)

// Table
case class FillTable(buildFromFile:String)



// Statuses
case object IsReady
case object StructureReady
case class TableHandlerStatus(statuses:Seq[Status])
case class TableStatus(id:Int, status:Status)
case object GetStatus

// Query
case class Query(q:(Int, Array[Float]), range:Double, probingScheme:String)
case class StructureQuery(q:Array[Float],range:Int)
case class QueryResult(items:ArrayBuffer[(Int, Array[Float])])

// Else
case class RunAccuracyTest(range:Double, k:Int, epochs:Int)
