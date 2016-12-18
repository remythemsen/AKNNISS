import java.io._

import LSH.structures.LSHStructure
import utils.tools.actorMessages._
import tools.status._
import akka.actor._
import utils.IO.ReducedFileParser
import utils.tools.{Cosine, Distance}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.util.Random

case class PerformanceConfig(dataSetSize:Int, functions:Int, numOfDim:Int, buildFromFile:String, knn:Int, tables:Int, range:Double, queries:String, measure:Distance, hashfunction:String, probingScheme:String, knnstructure:String)
case class StartPerformanceTest(config:PerformanceConfig)
object Program  extends App {
  // IDEA, Read config sets in from file, (One line is equal to one configuration)
  // make new performancetester actor (kill the old one)
  // repeat test

  // TEST CONFIGURATIONS TODO read this from file
  val dataSetSize = 1008935//39286 // The different datasizes (N)
  val queriesSetSize=2353
  val functions = 16// Number of functions run to create a hashvalue (m) (0-2 = hyper, 3-5 = x-poly)
  val kNearNeighbours = 30 // Number of neighbors to be compared for Recall measurements (k)
  val tables = 3 // Total Number of Tables (L)
  val range = 1.0 // Range boundary for retrieved points (cR)
  val queries = "data/accuracytest-queries-10k.data" // Set of Queries to be run
  val measure:Distance = Cosine
  val hashFunctions = "Hyperplane"
  val numOfDim = 256
  val buildFromFile = "data/descriptors-decaf-1m.data"
  val probingScheme = "None"
  val knnStructureLocation = "data/knnstructure"

  // Ip's of tablehandlers
  val ips = Array(
    "10.1.1.2"
    //,"10.1.1.3"
    //,"172.17.0.2"
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
      dataSetSize, functions, numOfDim, buildFromFile, kNearNeighbours, tables, range, queries, measure, hashFunctions, probingScheme, knnStructureLocation
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
  var KNNStructure = loadKNNStructure
  var lastQuerySent:Query = _

  var logFile = new File("data/logFile.log")
  var bufferWriter = new BufferedWriter(new FileWriter(logFile))
  var recallBuffer=new ArrayBuffer[Float]
  var recall=0.0f
  var candidateTotalSet=0


  def loadKNNStructure = {
    println("Loading KNN Structure")
    val objReader = new ObjectInputStream(new FileInputStream(config.knnstructure))
    //KNNStructure
    val hashMap = objReader.readObject.asInstanceOf[mutable.HashMap[Int,Array[(Int,Float)]]]
    objReader.close
    hashMap
  }

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
      println("Sending init message to tablehandlers")
      this.lshStructure ! InitializeTableHandlers

    }

    case Ready => {
      println("status received: Structure is Ready")
      this.lshStructureReady = true
      // Start the test
      self ! StartPerformanceTest
    }

    case QueryResult(res) => {
      println(res.length)
      for (r <- res)
        println(r._1)

      // test accuracy of result
      var knnSumDistances = 0.0f
      var LSHSumDistances = 0.0f
      val arrayOfDist = KNNStructure(lastQuerySent.q._1)
      for (i <- 0 until Program.kNearNeighbours) {
        LSHSumDistances += res(i)._2
        knnSumDistances += arrayOfDist(i)._2
      }
      recall += knnSumDistances / LSHSumDistances
      recallBuffer += recall
      candidateTotalSet += res.size

      // this.lastQuerySent VS: res
      // writeToFile:
      // (SEE what should be logged in facebook msg) (EXCEPT RUNNING TIMES)


      //  make new query,
      if (queryParser.hasNext) {
        this.lastQuerySent = Query(queryParser.next, config.range, config.probingScheme, config.measure)
        lshStructure ! this.lastQuerySent
      }
      else{
        //LOG File
        var mean=0.0f
        val standardDev={
          for(i<-0 until recallBuffer.size){
            mean+=recallBuffer(i)
          }
          mean+=mean/recallBuffer.size
          var variance=0.0
          for(i<-0 until recallBuffer.size){
            variance+=Math.pow((recallBuffer(i)-mean).toDouble,2)
          }
          variance+=variance/recallBuffer.size
          Math.sqrt(variance)
        }
        val avgRecall= recall/Program.queries.size

        var sb = new StringBuilder
        for (line <- Source.fromFile("data/logFile.txt").getLines()) {
          sb.append(line)
          sb.append(System.getProperty("line.separator"));
        }
        sb.append(Program.dataSetSize+","+Program.functions+","+ Program.kNearNeighbours+","+Program.tables+","+Program.range+","+","+Program.queriesSetSize+
          ","+avgRecall+","+standardDev+","+ candidateTotalSet +","+Program.measure+","+Program.numOfDim+","+Program.hashFunctions)
        sb.append(System.getProperty("line.separator"));

        // Write resulting set
        var file=new File("data/logFile.txt")
        var bufferWriter = new BufferedWriter(new FileWriter(file))
        bufferWriter.write(sb.toString())
        bufferWriter.close()
      }
    }
    case StartPerformanceTest => {
      println("Starting performance test, since tables are ready")
      // Run first accuracytest
      val q = Query(this.queryParser.next, config.range, config.probingScheme, config.measure)
      this.lastQuerySent = q
      this.lshStructure ! this.lastQuerySent
    }
  }

}
