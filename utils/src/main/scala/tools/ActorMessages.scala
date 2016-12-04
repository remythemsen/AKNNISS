package tools.actorMessages

import scala.collection.mutable.ArrayBuffer
trait ActorMessages {

}
case class FillTable(function:String, k:Int, seed:Long)
case object GetStatus
case class Query(q:Array[Float])
case class QueryResult(items:ArrayBuffer[(String, Array[Float])])
