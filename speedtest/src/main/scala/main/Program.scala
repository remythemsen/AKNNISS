import java.io._
import java.nio.file.{Files, Paths, StandardOpenOption}
import LSH.structures.LSHStructure
import utils.tools.actormessages._
import akka.actor._
import speedtest.SpeedConfig
import utils.tools.actormessages.{InitializeStructure, InitializeTableHandlers, Query, Ready}
import utils.IO.ReducedFileParser
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.util.Random

case class StartSpeedTest(config:SpeedConfig)

object Program extends App {
  // Get References to tablehandler nodes
  val ips = Source.fromFile("data/ips").getLines().next.split(" ") // Ip's of tablehandlers

  // table handler port
  val tbp = 2552

  val thsn = "TableHandlerSystem" // table handler Actor system name
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
  val system = ActorSystem("PerformanceTesterSystem")

  // Adding the performance tester actor!
  val speedTester = system.actorOf(Props(new SpeedTester(new sConfigParser("data/speedconfig"), tablehandlers, rnd.nextLong)), name = "SpeedTester")  // the local actor

  // Get the structure Ready
  speedTester ! InitializeStructure

}

class SpeedTester(configs:sConfigParser, tablehandlers:Array[String], seed:Long) extends Actor {
  val rnd = new Random(seed)

  // The Structure reference to table handlers
  val lshStructure:ActorRef = context.system.actorOf(Props(
    new LSHStructure(for {
      tableHandlerAddress <- tablehandlers
      tableHandler <- {
        Seq(context.actorSelection(tableHandlerAddress))
      }
    } yield tableHandler, context.system, context.self, rnd.nextLong)), name = "LSHStructure")

  var lshStructureReady = false

  var queryParser:ReducedFileParser = _
  var queryParserWarmUp:ReducedFileParser=_
  var lastQuerySent:Query = _

  var candidateTotalSet=0
  var sumOfUnfilteredCands = 0
  var testsProgress = 0 // 1 out of 5 tests finished
  var testProgress = 0.0 // current test is 22% new Random(seed)one
  val testCount = Source.fromFile(new File("data/speedconfig")).getLines().size

  // Current Config
  var warmupCount:Int = _
  var warmupProgress:Double = 0.0
  var warmupPercentile:Int = _
  var config:SpeedConfig = _
  var LSHBuildTime=0.0
  var queryTimeBuffer = new ArrayBuffer[Double]()
  val time=new Timer()
  val buildTimer  = new Timer()

  def receive = {

    // Starting or resetting the Structure
    case InitializeStructure => {
      // Loading first (or next) config
      this.config = configs.next

      this.lshStructureReady = false
      // Inform the LSHStructure to initialize it's tablehandlers
      println("Initializing or Re-initializing Structure ")

      this.buildTimer.play()
      this.lshStructure ! InitializeTableHandlers(
        config.hashfunction,
        config.tables,
        config.functions,
        config.numOfDim,
        config.buildFromFile,
        config.knn
      )

      this.queryParser = new ReducedFileParser(new File(config.queries))

      //parse warm up file
      this.queryParserWarmUp=new ReducedFileParser(new File(config.queriesWarmUp))
      this.warmupCount = queryParserWarmUp.size
      this.warmupPercentile = this.warmupCount / 100
    }

    case Ready => {
      //LSH structure build time
      LSHBuildTime = this.buildTimer.check()
      println("status received: Structure is Ready")
      this.lshStructureReady = true

      //Run warm up queries file
      while(queryParserWarmUp.hasNext){
         lshStructure ! Query(this.queryParserWarmUp.next, config.range, config.probingScheme, config.measure,config.knn, config.numOfProbes)
      }

      // Start the test
      self ! StartSpeedTest
    }

    case QueryResult(res, numOfUnfilteredCands) => {
      if(this.warmupProgress < this.warmupCount) {
        this.warmupProgress += 1.0
        if (this.warmupProgress % this.warmupPercentile == 0) {
          println("WarmUp Progress: " + ((this.warmupProgress / this.warmupCount) * 100).toInt + "%")
        }
      } else {
        queryTimeBuffer+= this.time.check()
        this.testProgress += 1.0
        this.sumOfUnfilteredCands+=numOfUnfilteredCands


        // Print progress
        if(testProgress % (config.queriesSetSize / 100) == 0) {
          println("test "+(this.testsProgress+1)+" out of "+this.testCount+" : " + ((testProgress / config.queriesSetSize) * 100).toInt + "%")
        }

        // Was this the last query for this config?
        if(!queryParser.hasNext) {

          val avgQueryTime= queryTimeBuffer.sum/config.queriesSetSize
          println("avgQueryTime = "+avgQueryTime)
          val qtimeVariance = {
            var tmp = 0.0
            for(qt <- queryTimeBuffer) {
              tmp+=(qt-avgQueryTime)*(qt-avgQueryTime)
            }
            tmp / queryTimeBuffer.size
          }
          val stdDev = Math.sqrt(qtimeVariance)

          var sb = new StringBuilder
          sb.append(config.dataSetSize+" ")
          sb.append(config.functions+" ")
          sb.append(config.knn+" ")
          sb.append(config.tables+" ")
          sb.append(config.range+" ")
          sb.append(config.queriesSetSize+" ")
          sb.append(avgQueryTime+" ")
          sb.append(stdDev+" ")
          sb.append(LSHBuildTime +" ")
          sb.append(config.measure+" ")
          sb.append(config.numOfDim+" ")
          sb.append(config.hashfunction+" ")
          sb.append(config.probingScheme+" ")
          sb.append({
            config.hashfunction match {
              case "Hyperplane" => config.functions * (config.functions+1) / 2
              case "Crosspolytope" => config.numOfProbes
            }
          } + " ")
          sb.append((sumOfUnfilteredCands / config.queriesSetSize) + " ")
          sb.append((((sumOfUnfilteredCands.toFloat / config.queriesSetSize.toFloat) / config.dataSetSize.toFloat) * 100) + " ")
          sb.append(System.getProperty("line.separator"))

          // Write resulting set
          Files.write(Paths.get("data/SpeedLogFile.log"), sb.toString.getBytes(), StandardOpenOption.APPEND);

          // RESET Counters
          this.LSHBuildTime = 0
          this.candidateTotalSet = 0
          this.queryTimeBuffer=ArrayBuffer.empty
          this.sumOfUnfilteredCands=0
          this.testsProgress += 1
          println("Speed Test "+this.testsProgress.toInt+" out of " + this.testCount + " has finished")

          this.testProgress = 0.0

          // Start next test !
          if(this.configs.hasNext)
            self ! InitializeStructure
          else
            context.system.terminate()

        } else {
          // Go ahead to next query!
          this.lastQuerySent = Query(queryParser.next, config.range, config.probingScheme, config.measure,config.knn, config.numOfProbes)
          this.time.play()
          lshStructure ! this.lastQuerySent
        }
      }

    }
    case StartSpeedTest => {
      println("Starting speed test, since tables are ready")
      // Run speed test
      this.time.play()
      val q = Query(this.queryParser.next, config.range, config.probingScheme, config.measure,config.knn, config.numOfProbes)

      this.lastQuerySent = q
      this.lshStructure ! this.lastQuerySent
    }
  }



}
