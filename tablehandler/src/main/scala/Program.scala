package tablehandler

import java.io.File

import LSH.hashFunctions.{HashFunction, Hyperplane}
import LSH.structures.HashTable
import akka.actor.Actor
import akka.actor._
import tools.status._
import utils.tools.actorMessages._
import utils.IO.Parser
import utils.tools.Cosine

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
  var tables:IndexedSeq[ActorRef] = IndexedSeq.empty
  var lshStructure:ActorRef = _

  var readyTables = 0
  var readyQueryResults = 0
  var queryResult = ArrayBuffer.empty

  var statuses:Array[Status] = _

  def receive = {
    case TableStatus(id, status) => {
      // Who sent the msg
      this.statuses(id) = status
      // Pass on updated tablestatus
      this.lshStructure ! TableHandlerStatus(statuses.toSeq)

    }

    case InitializeTables(hf, numOfTables, functions, numOfDim, seed, inputFile) => {
      println("TableHandler recieved Init message")
      // TODO Make ready for variable hashfunction
      this.statuses = new Array(numOfTables)
      val rnd = new Random(seed)
      this.lshStructure = sender

      this.tables = for {
        i <- 0 until numOfTables
        table <- {
          List(context.system.actorOf(Props(new Table(() => {
            hf match {
              case "Hyperplane" => {
                new Hyperplane(functions, () => new Random(rnd.nextLong), numOfDim)
              }
              case "Crosspolytope" => {
                //TODO Insert xpoly algo
                new Hyperplane(functions, () => new Random(rnd.nextLong), numOfDim)
              }
            }
          }, i)), name = "Table_"+i))
        }
      } yield table
      for(t <- this.tables) {
        t ! FillTable(inputFile)
      }
    }

    case QueryResult(queryResult) => {
      this.queryResult ++ queryResult
      this.readyQueryResults += 1
      if(this.readyQueryResults == tables.length-1) {

        this.lshStructure ! this.queryResult.distinct

        // reset query result and ready tables
        this.readyQueryResults = 0
        this.queryResult = ArrayBuffer.empty
      }
    }

    case Query(queryPoint, range) => {
      // go through each table
      for(t <- tables) {
        t ! Query(queryPoint, range)
      }
    }

  }
}

class Table(hf:() => HashFunction, tableId:Int) extends Actor {
  private var status:Status = NotReady
  private var table:HashTable = _
  private var tableHandler:ActorRef = _
  val id = tableId

  def receive: Receive = {

    // Initializes the TableActor
    case FillTable(buildFromFile) => {
      println("Table #"+id+" recieved message to start building")
      val parser = new Parser(new File(buildFromFile))

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
    case Query(q, range) => {
      sender ! {
        // Get all candidates in this table
        val cands = table.query(q)
        // get distinct, and remove outside of range results (false positives)
        val trimmedcands = cands.distinct.filter(x => Cosine.measure(x._2, q) <= range)
        QueryResult(trimmedcands)
      }
    }
  }
}

