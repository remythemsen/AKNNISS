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
import ExecutionContext.Implicits.global
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext
import scala.util.Random

object Program extends App {
  val system = ActorSystem("TableHandlerSystem")
  val tableHandler = system.actorOf(Props[TableHandler], name = "TableHandler")
  tableHandler ! "A tablehandler came alive"
}

class TableHandler extends Actor {
  var tables:IndexedSeq[ActorRef] = IndexedSeq.empty
  var readyTables = 0
  var queryResult = ArrayBuffer.empty
  var lshStructure:ActorRef = _

  def receive = {
    case InitializeTables(hf, k, seed) => {
      // TODO Make ready for variable hashfunction
      val rnd = new Random(seed)
      this.lshStructure = sender

      this.tables = for {
        i <- 0 until 2
        table <- List(context.system.actorOf(Props(new Table(() => new Hyperplane(k, () => new Random(rnd.nextLong)))), name = "Table"))
      } yield table
      for(t <- tables) {
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

class Table(hf:() => HashFunction) extends Actor {
  private var status:Status = NotReady
  private val parser = new Parser(new File("data/descriptors-decaf-random-sample-reduced.data"))
  private var table:HashTable = _
  private var tableHandler:ActorRef = _

  def receive: Receive = {

    // Initializes the TableActor
    case FillTable => {
      if(this.status.equals(NotReady)) {
        this.tableHandler = sender
        this.table = new HashTable(hf)
        Future {
          for (j <- 0 until parser.size) {
            status = InProgress(((j.toDouble / parser.size)*100).toInt)
            table += parser.next
          }
        } (ExecutionContext.Implicits.global) onSuccess {
          // Telling the handler about this being ready
          case _ => tableHandler ! Ready
        }
      } else {
        throw new Exception("Table was already initialized")
      }
    }

    // Returns the current Status
    case GetStatus => sender ! status

    // Returns a candidate set for query point
    case Query(q) => {
      sender ! {
        QueryResult(table.query(q))
      }
    }
  }
}

