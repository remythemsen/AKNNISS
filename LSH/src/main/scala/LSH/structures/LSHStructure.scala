package LSH.structures

import akka.actor._
import utils.tools.actorMessages._
import tools.status._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class LSHStructure(tbhs:IndexedSeq[ActorSelection], hashFunction:String, tableCount:Int, functionCount:Int, numOfDim:Int, seed:Long, inputFile:String, system:ActorSystem, owner:ActorRef) extends Actor {
  private val tableHandlers = tbhs
  private var queryResults:ArrayBuffer[(Int, Array[Float])] = new ArrayBuffer[(Int, Array[Float])] // TODO change out with cheap insert + traversal datatype
  private var queryPoint:(Int, Array[Float]) = _
  private var readyTableHandlers = 0
  private var readyResults = 0
  private var structureIsReady = false
  private var callingActor:ActorRef = owner
  private var tableHandlerStatuses:mutable.HashMap[Int, TableHandlerStatus] = new mutable.HashMap

  val rnd = new Random(seed)

  def receive = {
    case InitializeTableHandlers => {

      // Start all the tablebuilding!
      for(t <- this.tableHandlers) {
        t ! InitializeTables(hashFunction, tableCount / this.tableHandlers.length, functionCount, numOfDim, rnd.nextLong, inputFile)
      }
    }

    // Status update from table handlers
    case ths:TableHandlerStatus => {
      // which table handler did send it ?, update map with new set
      this.tableHandlerStatuses(sender.hashCode()) = ths

      // Format combined statuses
      // Tables: 3%  5%  3%  5%  3%  5%
      val sb = new StringBuilder
      sb.append("\t")
      val thss = tableHandlerStatuses.valuesIterator.toIndexedSeq
      for(ths <- thss) {
        ths match {
          case TableHandlerStatus(tableStatuses) => {
            for (ts <- tableStatuses) {
              ts match {
                case InProgress(progress) => {
                  sb.append(progress + "%\t")
                }
                case Ready => {
                  self ! Ready
                  sb.append("Ready\t")
                }
                case _ => {
                  sb.append("Not Ready\t")
                }
              }
            }
          }
        }
      }
      println(sb.toString)
    }

    // When ever a tablehandler finishes,
    // its checked whether all other tablehandlers has also finished
    case Ready => {
      if(!this.structureIsReady) {
        var res = true
        for(s <- tableHandlerStatuses) {
          s._2 match {
            case TableHandlerStatus(statuses)  => {
              for (ts <- statuses) {
                ts match {
                  case InProgress(p) => res = false
                  case NotReady => res = false
                  case Ready => {}
                }
              }
            }
          }
        }
        if(res) {
          //send ready notice to performanceActor
          this.structureIsReady = true
          callingActor ! Ready
        }
      }
    }
    case Query(queryPoint, range, probingScheme) => {
      // Set the query Point
      // For each tablehandlers, send query request
      for (th <- tableHandlers) {
        th ! Query(queryPoint, range, probingScheme)
      }
    }
    case QueryResult(queryPoints) => {
      // concat result
      this.queryResults = this.queryResults ++ queryPoints
      this.readyResults += 1

      // check if all results are in
      if(readyResults == tableCount) {
        // The last result just came in!

        // Send result to query owner/parent? // TODO sort this!!!
        this.callingActor ! QueryResult(this.queryResults)

        // reset counter, query, and results
        this.readyResults = 0

        this.queryResults = new ArrayBuffer[(Int, Array[Float])]
      }
    }
  }
}

