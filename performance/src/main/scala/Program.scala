import java.io.File

import LSH.structures.LSHStructure
import utils.tools.actorMessages._
import tools.status._
import akka.actor._
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import akka.actor.{ActorSystem, Props}
import utils.IO.Parser
import akka.pattern.ask

import scala.collection.GenTraversableOnce

object Program  extends App {
  val system = ActorSystem("PerformanceTesterSystem")
  val performanceTester = system.actorOf(Props[PerformanceTester], name = "PerformanceTester")  // the local actor

  // Launch the first test
  performanceTester ! InitializeStructure(0.4)

  // testing whether it runs
  val i = 0
  implicit val timeout = Timeout(2.minutes)
  while(i < 1000) {
    println("Asking performancetester for status")
    var res = performanceTester ? GetStatus
    println(Await.result(res, 2.minutes))//.asInstanceOf[GenTraversableOnce[Future[Status]]])
    Thread.sleep(2000)
  }

  //performanceTester ! RunAccuracyTest

  // TODO Figure out if we need to restart the JVM instead of resetting
  // TODO Make RESET functionality if needed

}

class PerformanceTester extends Actor {
  // Ip's of tablehandlers
  val ips = IndexedSeq(
    "172.19.0.2"
    ,"172.19.0.3"
    ,"172.19.0.4"
  )

  // table handler port
  val tbp = 2552

  val thsn = "TablehandlerSystem" // table handler Actor systemname
  val systemName = "akka.tcp://"+thsn+"@"
  val actorPath = "/user/TableHandler"

  // The parser of the queries file
  val parser = new Parser(new File("data/queries.data"))

  var lshStructure:ActorRef = _
  var lshStructureReady = false

  def receive = {
    case Ready => {
      this.lshStructureReady = true
    }

    case GetStatus => {
      println("Asking LSHstructrure for status")
      implicit val timeout = Timeout(2.minutes)
      val res = lshStructure ? GetStatus
      sender ! res
    }

    case QueryResult(res) => {
      // test accuracy of result
      //  log results,
      //  make new query,
      lshStructure ! Query(parser.next._2)
    }
    case RunAccuracyTest(x) => { // TODO Add start object containing params
      // If the LSHStructure is ready and parser has next, go ahead
      if(this.lshStructureReady)
        lshStructure ! Query(parser.next._2)
      else
        lshStructure ! GetStatus
    }
    case InitializeStructure(range) => {
      this.lshStructureReady = false
      this.lshStructure = context.system.actorOf(Props(new LSHStructure(for {
        ip <- ips
        tableHandler <- Seq(context.actorSelection(systemName+ip+actorPath))
      } yield tableHandler, ips.length*2, context.self, range)), name = "LSHStructure") // each tablehandler has two tables
    }
  }
}
