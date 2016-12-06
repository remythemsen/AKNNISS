package utils.tools.actorMessages

import scala.collection.mutable.ArrayBuffer
trait ActorMessages {

}
case object FillTable
case object GetStatus
case class Query(q:Array[Float])
case class StructureQuery(q:Array[Float],range:Int)
case class QueryResult(items:ArrayBuffer[(String, Array[Float])])
case class RunAccuracyTest(params:String)
case object Initialize
case class InitializeStructure(range:Double)
case object IsReady
case object StructureReady
case class InitializeTables(hf:String, k:Int, seed:Long)
