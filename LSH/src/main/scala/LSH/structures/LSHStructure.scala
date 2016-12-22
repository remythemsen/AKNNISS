package LSH.structures

import akka.actor._
import utils.tools.actormessages._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class LSHStructure(tbhs:IndexedSeq[ActorSelection], system:ActorSystem, owner:ActorRef, seed:Long) extends Actor {
  private val tableHandlers = tbhs
  private var tables: Int = _
  private var queryResults: ArrayBuffer[(Int, Float)] = new ArrayBuffer[(Int, Float)]
  // TODO change out with cheap insert + traversal datatype
  private var readyResults = 0
  private var structureIsReady = false
  private val callingActor: ActorRef = owner
  private var tableHandlerStatuses: mutable.HashMap[Int, TableHandlerStatus] = _
  private var numberOfknn: Int = _
  private var totalNumOfUnfilteredCands: Int = _

  val rnd = new Random(seed)


  def receive:Unit = {
    case InitializeTableHandlers(
    hashFunction, tableCount, functionCount, numOfDim, inputFile, knn
    ) =>
      this.structureIsReady = false
      this.tableHandlerStatuses = new mutable.HashMap
      this.numberOfknn = knn

      // Setting the new number of tables to be used
      this.tables = tableCount

      // Start all the tablebuilding!
      for (t <- 0 until this.tables) {
        tableHandlers(t) ! InitializeTables(hashFunction, 1, functionCount, numOfDim, rnd.nextLong, inputFile)
      }

    // Status update from table handlers
    case ths: TableHandlerStatus =>
      // which table handler did send it ?, update map with new set
      this.tableHandlerStatuses(sender.hashCode()) = ths

      // Format combined statuses
      // Tables: 3%  5%  3%  5%  3%  5%
      val sb = new StringBuilder
      sb.append("\t")
      val thss = tableHandlerStatuses.valuesIterator
      for (ths <- thss) {
        ths match {
          case TableHandlerStatus(tableStatuses) =>
            for (ts <- tableStatuses) {
              ts match {
                case InProgress(progress) => sb.append(progress + "%\t")
                case Ready =>
                  self ! Ready
                  sb.append("Ready\t")
                case _ => sb.append("Not Ready\t")
              }
            }
        }
      }
      println(sb.toString)

    // When ever a tablehandler finishes,
    // its checked whether all other tablehandlers has also finished
    case Ready =>
      if (!this.structureIsReady) {
        var res = true
        for (s <- tableHandlerStatuses) {
          s._2 match {
            case TableHandlerStatus(statuses) =>
              for (ts <- statuses) {
                ts match {
                  case InProgress(_) => res = false
                  case NotReady => res = false
                  case Ready =>
                }
              }
          }
        }
        if (res) {
          //send ready notice to performanceActor
          this.structureIsReady = true
          callingActor ! Ready
        }
      }

    case Query(queryPoint, range, probingScheme, distMeasure, k, numOfProbes) =>
      // Set the query Point
      // For each tablehandlers, send query request
      for (t <- 0 until this.tables) {
        this.tableHandlers(t) ! Query(queryPoint, range, probingScheme, distMeasure, k, numOfProbes)
      }

    case QueryResult(queryPoints, numOfUnfilteredCands) =>

      // concat result
      this.queryResults = this.queryResults ++ queryPoints
      this.readyResults += 1
      this.totalNumOfUnfilteredCands += numOfUnfilteredCands

      // check if all results are in
      if (readyResults == this.tables) {
        // The last result just came in!

        // Returns K out of the K*L candidates
        // Skips the first one, since that is the query point itself!
        this.callingActor ! QueryResult(this.queryResults.distinct.sortBy(x => x._2).slice(1, this.numberOfknn + 1), totalNumOfUnfilteredCands)

        // reset counter, query, and results
        this.readyResults = 0
        this.totalNumOfUnfilteredCands = 0

        this.queryResults = new ArrayBuffer[(Int, Float)]
      }
  }
}

