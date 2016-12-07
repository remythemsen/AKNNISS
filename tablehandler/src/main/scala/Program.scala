package tablehandler

import java.io.File
import LSH.hashFunctions.{HashFunction, Hyperplane}
import LSH.structures.HashTable
import akka.actor.Actor
import akka.actor._
import tools.status._
import utils.tools.actorMessages._
import utils.IO.Parser
import scala.concurrent._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global

object Program extends App {
  val system = ActorSystem("TableHandlerSystem")
  val tableHandler = system.actorOf(Props[TableHandler], name = "TableHandler")
  tableHandler ! "A tablehandler came alive"
}

class TableHandler extends Actor {
  var numberOfTables = 2
  var tables:IndexedSeq[ActorRef] = IndexedSeq.empty
  var readyTables = 0
  var queryResult = ArrayBuffer.empty
  var lshStructure:ActorRef = _
  // TODO Get number of tables for tablehandler
  var statuses:Array[Status] = new Array(this.numberOfTables)

  def receive = {
    case TableStatus(id, status) => {
      // Who sent the msg
      this.statuses(id) = status
      // Pass on updated tablestatus
      this.lshStructure ! TableHandlerStatus(statuses.toSeq)
    }

    case InitializeTables(hf, k, seed, numOfDim) => {
      println("TableHandler recieved Init message")
      // TODO Make ready for variable hashfunction
      val rnd = new Random(seed)
      this.lshStructure = sender

      this.tables = for {
        i <- 0 until this.numberOfTables
        table <- List(context.system.actorOf(Props(new Table(() => new Hyperplane(k, () => new Random(rnd.nextLong), numOfDim), i)), name = "Table_"+i))
      } yield table
      for(t <- this.tables) {
        t ! FillTable
      }
    }

    case QueryResult(queryResult) => {
      this.queryResult ++ queryResult
      this.readyTables += 1
      if(this.readyTables == tables.length-1) {
        // send distinct result to querysender
        this.lshStructure ! this.queryResult

        // reset queryresult and ready tables
        this.readyTables = 0
        this.queryResult = ArrayBuffer.empty
      }
    }

    case Query(queryPoint) => {
      // go through each table
      for(t <- tables) {
        t ! Query(queryPoint)
      }
    }

  }
}

class Table(hf:() => HashFunction, tableId:Int) extends Actor {
  private var status:Status = NotReady
  private val parser = new Parser(new File("data/descriptors-decaf-random-sample-reduced.data"))
  private var table:HashTable = _
  private var tableHandler:ActorRef = _
  val id = tableId

  def receive: Receive = {

    // Initializes the TableActor
    case FillTable => {
      println("Table #"+id+" recieved message to start building")
      if(this.status.equals(NotReady)) {
        this.tableHandler = sender
        this.table = new HashTable(hf)
        Future {
          for (j <- 0 until parser.size) {
            status = InProgress(((j.toDouble / parser.size)*100).toInt)
            table += parser.next
            if(j % 400 == 0) {
              tableHandler ! TableStatus(this.id, this.status)
            }
          }
        } (ExecutionContext.Implicits.global) onSuccess {
          // Telling the handler about this being ready
          case _ => {
            status = Ready
            tableHandler ! TableStatus(this.id, Ready)
          }
        }
      } else {
        throw new Exception("Table was already initialized")
      }
    }

    // Returns a candidate set for query point
    case Query(q) => {
      sender ! {
        QueryResult(table.query(q))
      }
    }
  }
}

