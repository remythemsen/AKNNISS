package LSH.actors

import LSH.hashFunctions.HashFunction
import LSH.structures.HashTable
import akka.actor.Actor
import utils.IO.Parser

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext

case class FillTable(parser:Parser)
case object GetStatus
case class Query(q:Array[Float])

case class QueryResult(items:ArrayBuffer[(String, Array[Float])])

trait Status
case object Ready extends Status
case class InProgress(progress:Int) extends Status
case object NotReady extends Status

class TableActor(hf:() => HashFunction) extends Actor {
  // TODO Add L Tables to each Table Actor

  private val table = new HashTable(hf)
  private var status:Status = NotReady

  def receive: Receive = {

    // Initializes the TableActor
    case FillTable(parser) => {
      Future {
        for (j <- 0 until parser.size) {
          status = InProgress(((j.toDouble / parser.size)*100).toInt)
          table += parser.next
        }
      } (ExecutionContext.Implicits.global) onSuccess {
        case _ => status =  Ready
      }
    }

    // Returns the current Status
    case GetStatus => sender ! status

    // Returns a candidate set for query point
    case Query(q) => {
      sender ! {
        if(status != Ready) Left("Table actor is not ready yet")
        else Right(QueryResult(table.query(q)))
      }
    }
  }
}
