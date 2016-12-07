package utils.tools.actorMessages

import tools.status.Status

import scala.collection.mutable.ArrayBuffer
trait ActorMessages {

}
case object FillTable
case object GetStatus
case class Query(q:Array[Float])
case class StructureQuery(q:Array[Float],range:Int)
case class QueryResult(items:ArrayBuffer[(String, Array[Float])])
case class RunAccuracyTest(params:String)
case class Initialize(numOfDim:Int)
case class InitializeStructure(range:Double, numOfDim:Int)
case object IsReady
case object StructureReady
case class InitializeTables(hf:String, k:Int, seed:Long, numOfDim:Int)
case class TableHandlerStatus(statuses:Seq[Status])
case class TableStatus(id:Int, status:Status)
