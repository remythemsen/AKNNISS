package LSH.structures
import utils.tools.{Cosine, Distance}
import akka.actor._
import utils.tools.actorMessages._
import tools.status._
import akka.pattern.ask
import akka.util.Timeout

import scala.collection.GenTraversableOnce
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Await, Future}

class LSHStructure(tbhs:IndexedSeq[ActorSelection], tableCount:Int, owner:ActorRef, r:Double) extends Actor {
  private val tableHandlers = tbhs
  private val L = tableCount
  private var range = r
  private var queryResults:ArrayBuffer[(String, Array[Float])] = _ // TODO change out with cheap insert + traversal datatype
  private var queryPoint:Array[Float] = _
  private var readyTableHandlers = 0
  private var readyResults = 0
  private var callingActor:ActorRef = owner

  def receive = {
    // Get a status of the structure
    case GetStatus => {
      val statuses:ArrayBuffer[Future[Status]] = ArrayBuffer.empty
      implicit val timeout = Timeout(2.minutes)
      for(th <- this.tableHandlers)
        statuses ++ (th ? GetStatus).asInstanceOf[GenTraversableOnce[Future[Status]]]

      sender ! Await.result(Future.sequence(statuses), 2.minutes)
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

