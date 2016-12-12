import java.io.File

import LSH.structures.LSHStructure
import utils.tools.actorMessages._
import tools.status._
import akka.actor._
import utils.IO.ReducedFileParser

import scala.util.Random

case class PerformanceConfig(dataSetSize:Int, functions:Int, numOfDim:Int, buildFromFile:String, knn:Int, tables:Int, range:Double, queries:String, measure:String, hashfunction:String, probingScheme:String)
case class StartPerformanceTest(config:PerformanceConfig)
object Program  extends App {
  // IDEA, Read config sets in from file, (One line is equal to one configuration)
  // make new performancetester actor (kill the old one)
  // repeat test

  // TEST CONFIGURATIONS TODO read this from file
  val dataSetSize = 39920 // The different datasizes (N)
  val functions = 8// Number of functions run to create a hashvalue (m) (0-2 = hyper, 3-5 = x-poly)
  val kNearNeighbours = 10 // Number of neighbors to be compared for Recall measurements (k)
  val tables = 3 // Total Number of Tables (L)
  val range = 1.0 // Range boundary for retrieved points (cR)
  val queries = "data/queries.data" // Set of Queries to be run
  val measure = "Cosine"
  val hashFunctions = "Crosspolytope"
  val numOfDim = 256
  val buildFromFile = "data/descriptors-decaf-random-sample-reduced.data"
  val probingScheme = "Crosspolytope"

  // Ip's of tablehandlers
  val ips = Array(
    "172.20.0.2"
    ,"172.20.0.3"
    ,"172.20.0.4"
  )

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


  // make the tester system
  val system = ActorSystem("PerformanceTesterSystem")
  val performanceTester = system.actorOf(Props(new PerformanceTester(
    new PerformanceConfig(
      dataSetSize, functions, numOfDim, buildFromFile, kNearNeighbours, tables, range, queries, measure, hashFunctions, probingScheme
    ), tablehandlers)
  ), name = "PerformanceTester")  // the local actor

  // Better random seed ??
  val rnd = new Random(System.currentTimeMillis())

  // Get the structure Ready
  performanceTester ! InitializeStructure(rnd.nextLong())

  // Launch the performanceTester
  //performanceTester ! StartPerformanceTest


  // TODO Figure out if we need to restart the JVM instead of resetting
  // TODO Make RESET functionality if needed

}

class PerformanceTester(pConfig:PerformanceConfig, tablehandlers:Array[String]) extends Actor {

  val config = pConfig
  var lshStructure:ActorRef = _
  var lshStructureReady = false
  val queryParser = new ReducedFileParser(new File(config.queries))

  def receive = {

    // Starting up the Structure

    case InitializeStructure(seed) => {
      val rnd = new Random(seed)

      this.lshStructureReady = false
      // Making the structure
      this.lshStructure = context.system.actorOf(Props(new LSHStructure(for {
        tableHandlerAddress <- tablehandlers
        tableHandler <- {
          Seq(context.actorSelection(tableHandlerAddress))
        }
      } yield tableHandler, config.hashfunction,config.tables, config.functions, config.numOfDim, rnd.nextLong, config.buildFromFile, context.system, context.self)), name = "LSHStructure") // each tablehandler has two tables


      // Inform the LSHStructure to initialize it's tablehandlers
      this.lshStructure ! InitializeTableHandlers

    }

    case Ready => {
      println("status recieved: Structure is Ready")
      this.lshStructureReady = true
      // Start the test
      self ! StartPerformanceTest
    }

    case QueryResult(res) => {
      // test accuracy of result
      //  log results,
      //  make new query,
      if(queryParser.hasNext)
        lshStructure ! Query(queryParser.next._2, config.range, config.probingScheme)
    }

    case StartPerformanceTest => {
      println("Starting performance test, since tables are ready")
      // Run first accuracytest
      this.lshStructure ! Query(this.queryParser.next._2, config.range, config.probingScheme)

    }
    case RunAccuracyTest(queryFile, range, k) => { // TODO Add start object containing params

      // If the LSHStructure is ready and parser has next, go ahead
      if(this.lshStructureReady)
        // TODO Make entire test loop here
        lshStructure ! Query(queryParser.next._2, config.range, config.probingScheme)
      else
        println("Structure is not ready yet!")
    }
  }
}
