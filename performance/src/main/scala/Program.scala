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

import scala.util.Random

object Program  extends App {
  val system = ActorSystem("PerformanceTesterSystem")
  val performanceTester = system.actorOf(Props[PerformanceTester], name = "PerformanceTester")  // the local actor

  // Launch the first test
  performanceTester ! Initialize
  performanceTester ! RunAccuracyTest

  // TODO Figure out if we need to restart the JVM instead of resetting
  // TODO Make RESET functionality if needed

}

class PerformanceTester extends Actor {
  // Ip's of tablehandlers
  val ips = Seq(
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
    case Initialize => {

    }
    case Ready => {
      this.lshStructureReady = true
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
    case Initialize => {
      this.lshStructureReady = false
      val lshs = new LSHStructure(for {
        ip <- ips
        tableHandler <- context.actorSelection(systemName+ip+actorPath)
      } yield tableHandler, ips.length*2) // each tablehandler has two tables

      this.lshStructure = context.system.actorOf(Props(lshs), name = "LSHStructure")
    }
  }
}
