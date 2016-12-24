package tablehandler

import java.io.File

import LSH.hashFunctions.{CrossPolytope, HashFunction, Hyperplane}
import LSH.structures.HashTable
import akka.actor._
import akka.pattern.gracefulStop
import utils.IO.ReducedFileParser
import utils.tools.QuickSelect
import utils.tools.actormessages._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Random

object Program extends App {
  val system = ActorSystem("TableHandlerSystem")
  val tableHandler = system.actorOf(Props[TableHandler], name = "TableHandler")
  tableHandler ! "A tablehandler came alive"
}

class TableHandler extends Actor {
  var tables:IndexedSeq[ActorRef] = IndexedSeq.empty
  var lshStructure:ActorRef = _
  // this increments the id for table actors made
  var tableActorCounter = 0

  var readyTables = 0
  var readyQueryResults = 0
  var queryResult = new ArrayBuffer[(Int, Float)]()

  var totalAmountOfCands:Int = _

  var statuses:Array[Status] = _

  def receive = {
    // A Table sent a status update
    case TableStatus(id, status) =>
      // Who sent the msg
      this.statuses(id) = status
      // Pass on updated tablestatus
      this.lshStructure ! TableHandlerStatus(statuses)



    case InitializeTables(hf, numOfTables, functions, numOfDim, seed, inputFile) =>
      println("TableHandler recieved Init message")

      var shouldBeStopped = new ArrayBuffer[Future[Boolean]]()
      if(this.tables.length > 0) {

        // Clean up old actors
        for (t <- this.tables) {
          shouldBeStopped += gracefulStop(t, 10.hours)


          Await.ready(Future.sequence(shouldBeStopped), 10.hours)
          shouldBeStopped = ArrayBuffer.empty
        }
      }

      this.statuses = new Array(numOfTables)

      val rnd = new Random(seed)
      this.lshStructure = sender

      this.tables = for {
        i <- 0 until numOfTables
        table <- {
          List(context.system.actorOf(Props(new Table(() => {
            hf match {
              case "Hyperplane" => Hyperplane(functions, () => new Random(rnd.nextLong), numOfDim)
              case "Crosspolytope" => CrossPolytope(functions, () => new Random(rnd.nextLong), numOfDim)
            }
          }, i)), name = "Table_"+ {
            tableActorCounter+=1
            tableActorCounter
          }))
        }
      } yield table
      for(t <- this.tables) {
        t ! FillTable(inputFile)
      }


    case QueryResult(newQueryResult, numOfCands) =>
      this.queryResult ++= newQueryResult

      this.totalAmountOfCands+=numOfCands

      this.readyQueryResults += 1
      if(this.readyQueryResults == tables.length) {

        this.lshStructure ! QueryResult({
          if(this.tables.length == 1) this.queryResult
          else { this.queryResult.distinct }
        }, totalAmountOfCands)

        // reset query result and ready tables
        this.readyQueryResults = 0
        this.totalAmountOfCands = 0
        // TODO Clear the array more efficiently
        this.queryResult = ArrayBuffer.empty
      }


    case Query(queryPoint, range, probingScheme, distMeasure, k, numOfProbes) =>
      // go through each table
      for(t <- tables) {
        t ! Query(queryPoint, range, probingScheme, distMeasure, k, numOfProbes)
      }


  }
}

class Table(hf:() => HashFunction, tableId:Int) extends Actor {
  private var status:Status = NotReady
  private var table:HashTable = new HashTable(hf)
  private var tableHandler:ActorRef = _
  val id:Int = tableId

  def receive: Receive = {

    // Initializes the TableActor
    case FillTable(buildFromFile) =>
      println("Table #" + id + " recieved message to start building")
      val parser = new ReducedFileParser(new File(buildFromFile))

      if (this.status.equals(NotReady)) {
        this.tableHandler = sender
        for (j <- 0 until parser.size) {
          status = InProgress(((j.toDouble / parser.size) * 100).toInt)
          this.table += parser.next
          if (j % 400 == 0) {
            tableHandler ! TableStatus(this.id, this.status)
          }
        }
        // Telling the handler about this being ready
        status = Ready
        tableHandler ! TableStatus(this.id, Ready)
      }


    // Returns a candidate set for query point
    case Query(q, range, probingScheme, distance, k, numOfProbes) =>

      sender ! {

        // Get all candidates in this table
        val cands:ArrayBuffer[(Int, Array[Float])] = table.mpQuery(q._2, range, probingScheme, numOfProbes)

        // measure distances:
        val cwithDists:ArrayBuffer[(Int, Float)] = for {
          c <- cands
          cp <- Array((c._1, distance.measure(c._2, q._2)))
        } yield cp


        // get distinct, and remove outside of range results (false positives)
        val qsk = {
          if(k > cwithDists.size)
            cwithDists.size-1
          else
            k-1
        }

        if(cwithDists.length < 1) {
          println(cwithDists.length)
          println(q._1)
        }
        val kthDist = QuickSelect.quickSelect(cwithDists, qsk)._2
        val trimmedcands:ArrayBuffer[(Int,Float)] = cwithDists.filter(x => x._2 <= kthDist)

        QueryResult(trimmedcands, cands.size)
      }
  }

}

