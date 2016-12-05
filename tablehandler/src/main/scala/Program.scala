package tablehandler

import java.io.File
import LSH.hashFunctions.Hyperplane
import LSH.structures.HashTable
import akka.actor.Actor
import akka.actor._
import tools.status._
import utils.tools.actorMessages._
import utils.IO.Parser
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext
import scala.util.Random

object Program extends App {
  val system = ActorSystem("TableHandlerSystem")
  val tableHandler = system.actorOf(Props[TableHandler], name = "TableHandler")
  tableHandler ! "A tablehandler came alive"
}

class TableHandler extends Actor {
  var tables:IndexedSeq[ActorSelection] = _

  def receive = {
    case InitializeTable(hf, k, seed) => {
      this.tables = for {
        i <- 0 until 2
        table <- context.system.actorOf(Props[Table], name = "Table")
      } yield table
      for(t <- tables) {
        t ! FillTable(hf, k, seed)
      }
    }

    case Query(queryPoint) => {
      // go through each table

    }
  }
}

class Table extends Actor {
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
          table1 += parser.next
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

