package model

import LSH.structures.LSHStructure
import akka.actor._
import utils.tools.actormessages._
import utils.tools.actormessages.Query
import utils.tools._

import akka.util.Timeout
import scala.io.Source
import scala.util.Random
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

object LSHS {
  // Get References to tablehandler nodes
  val ips = Source.fromFile("data/ips").getLines().next.split(" ") // Ip's of tablehandlers

  // table handler port
  val tbp = 2552

  val thsn = "TableHandlerSystem" // table handler Actor systemname
  val systemName = "akka.tcp://"+thsn+"@"
  val actorPath = "/user/TableHandler"

  val tablehandlers = for {
    ip <- ips
    tableHandlerAddress <- {
      Array(systemName+ip+":"+tbp+actorPath)
    }
  } yield tableHandlerAddress


  // TODO Better random seed ??
  val rnd = new Random(System.currentTimeMillis())

  // make the tester system
  val system = ActorSystem("DemoSystem")

  // Adding the demo  actor!
  val demo = system.actorOf(Props(new Demo(tablehandlers, rnd.nextLong)), name = "Demo")  // the local actor

  // Get the structure Ready
  demo ! InitializeStructure

  def makeQuery(qp:(Int, Array[Float])):ArrayBuffer[(Int, Float)] = {
    //ArrayBuffer((1214, 0.21f), (1239, 0.20f), (1224, 0.11f))
    implicit val timeout = Timeout(5 hours)
    val fut = this.demo ? Query(qp, 1.0, "Hyperplane", Cosine, 30, 2)
    Await.result(fut, 1 hour).asInstanceOf[ArrayBuffer[(Int, Float)]]
  }
}

class Demo(tablehandlers:Array[String], seed:Long) extends Actor {
  val rnd = new Random(seed)
  var qSender:ActorRef = _

  // The Structure reference to table handlers
  val lshStructure:ActorRef = context.system.actorOf(Props(
    new LSHStructure(for {
      tableHandlerAddress <- tablehandlers
      tableHandler <- {
        Seq(context.actorSelection(tableHandlerAddress))
      }
    } yield tableHandler, context.system, context.self, rnd.nextLong)), name = "LSHStructure")

  var lshStructureReady = false

  val timer = new Timer

  def receive = {

    // Starting or resetting the Structure
    case InitializeStructure => {
      this.lshStructureReady = false
      // Inform the LSHStructure to initialize it's tablehandlers
      println("Initializing or Re-initializing Structure ")
      this.lshStructure ! InitializeTableHandlers(
        "Hyperplane",
        4, // Only one table per Handler
        8,
        256,
        "data/descriptors-decaf-random-sample-reduced.data",
        30
      )

    }

    case Ready => {
      println("status received: Structure is Ready")
      this.lshStructureReady = true
    }

    case QueryResult(res, numOfAccessedObjects) => {
      this.sender ! res
    }

    case Query(qp, range, pbsch, measure, knn, numOfProbs) => {
      println("QUERY RECIEVED!?")
      if(this.lshStructureReady) {
        this.qSender = context.sender()
        this.lshStructure ! Query(qp, range, pbsch, measure, knn, numOfProbs)
      }
      else {
        println("Structure was not ready yet!")
      }
    }
  }
}

