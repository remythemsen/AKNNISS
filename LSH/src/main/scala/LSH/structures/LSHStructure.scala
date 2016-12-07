package LSH.structures

import utils.tools.Cosine
import akka.actor._
import utils.tools.actorMessages._
import tools.status._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class LSHStructure(tbhs:IndexedSeq[ActorSelection], tableCount:Int, system:ActorSystem, owner:ActorRef, r:Double) extends Actor {
  private val tableHandlers = tbhs
  private val L = tableCount
  private var range = r
  private var queryResults:ArrayBuffer[(String, Array[Float])] = _ // TODO change out with cheap insert + traversal datatype
  private var queryPoint:Array[Float] = _
  private var readyTableHandlers = 0
  private var readyResults = 0
  private var callingActor:ActorRef = owner
  private var tableHandlerStatuses:mutable.HashMap[Int, TableHandlerStatus] = new mutable.HashMap


  // TODO Fix the randomness!!!!
  private val rnd = new Random()


  def receive = {
    case Initialize(numOfDim) => {
      // Start all the tablebuilding!
      for(t <- this.tableHandlers) {
        t ! InitializeTables("Hyperplane", 10, this.rnd.nextLong, numOfDim)
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
                case Ready => sb.append("Ready\t")
              }
            }
          }
        }
      }
      println(sb.toString)
    }

    // When ever a table finishes, it should message the structure that it did
    case Ready => {
      this.readyTableHandlers+=1
      if(readyTableHandlers == L) {
        //send ready notice to performanceActor
        callingActor ! Ready
      }
    }
    case StructureQuery(queryPoint, range) => {
      // Set the query Point
      this.queryPoint = queryPoint
      this.range = range
      // For each tablehandlers, send query request
      for (th <- tableHandlers) {
        th ! Query(queryPoint)
      }
    }
    case QueryResult(queryPoints) => {
      // concat result
      this.queryResults ++ queryPoints
      this.readyResults += 1

      // check if all results are in
      if(readyResults == L-1) {
        // The last result just came in!
        // Remove > range, distinct, sort
        val trimmedRes = queryResults.distinct.filter(x => Cosine.measure(x._2, queryPoint) <= this.range)

        // Send result to query owner/parent?
        this.callingActor ! QueryResult(trimmedRes)

        // reset counter, query, and results
        this.readyResults = 0
        this.queryPoint = new Array[Float](0) // nothing
        queryResults = new ArrayBuffer[(String, Array[Float])]
      }
    }
  }
}

