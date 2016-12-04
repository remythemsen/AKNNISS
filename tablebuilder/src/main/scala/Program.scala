package tablebuilder
import java.io.File

import LSH.hashFunctions.{HashFunction, Hyperplane}
import LSH.structures.HashTable
import akka.actor.Actor
import akka.actor._
import com.sun.org.apache.xerces.internal.parsers.CachingParserPool
import tools.status._
import tools.actorMessages._
import utils.IO.Parser

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext
import scala.util.Random


object TableBuilder extends App {
  val system = ActorSystem("TableBuilderSystem")
  val remoteActor = system.actorOf(Props[TableBuilder], name = "TableBuilder")
  remoteActor ! "A Tablebuilder came alive"
}

class TableBuilder extends Actor {
  private var status:Status = NotReady
  val parser = new Parser(new File("data/descriptors-decaf-random-sample-reduced.data"))
  var table:HashTable = _

  def receive: Receive = {

    // Initializes the TableActor
    case FillTable(hf, k, seed) => {
      table = new HashTable(() => hf match {
        case "Hyperplane" => new Hyperplane(k, () => new Random(seed))
        case _ => throw new Exception("Unknown hashfunction")
      })

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

