import java.io._
import java.nio.file.{Files, Paths, StandardOpenOption}
import LSH.structures.LSHStructure
import utils.tools.actorMessages._
import tools.status._
import akka.actor._
import utils.IO.ReducedFileParser
import utils.tools.{Cosine, Distance, Euclidean, Hamming}
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.util.Random

case class StartPerformanceTest(config:PerformanceConfig)

object Program extends App {
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
  val system = ActorSystem("PerformanceTesterSystem")

  // Adding the performance tester actor!
  val performanceTester = system.actorOf(Props(new PerformanceTester(new pConfigParser("data/pfconfig"), tablehandlers, rnd.nextLong)), name = "PerformanceTester")  // the local actor

  // Get the structure Ready
  performanceTester ! InitializeStructure

}

class PerformanceTester(configs:pConfigParser, tablehandlers:Array[String], seed:Long) extends Actor {
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
  var KNNStructure:mutable.HashMap[Int,Array[(Int,Float)]] = _
  var lastQuerySent:Query = _

  // Keeping state on test results
  var recallBuffer=new ArrayBuffer[Float]
  var hammingDists=new ArrayBuffer[Int]
  var recall=0.0f
  var candidateTotalSet=0

  var testsProgress = 0 // 1 out of 5 tests finished
  var testProgress = 0.0 // current test is 22% dnew Random(seed)one
  val testCount = Source.fromFile(new File("data/pfconfig")).getLines().size

  // Current Config
  var config:PerformanceConfig = _


  def receive = {

    // Starting or resetting the Structure
    case InitializeStructure => {
      // Loading first (or next) config
      this.config = configs.next

      this.lshStructureReady = false
      // Inform the LSHStructure to initialize it's tablehandlers
      println("Initializing or Re-initializing Structure ")
      this.lshStructure ! InitializeTableHandlers(
        config.hashfunction,
        tablehandlers.length, // Only one table per Handler
        config.functions,
        config.numOfDim,
        rnd.nextLong(),
        config.buildFromFile,
        config.knn
      )

      this.queryParser = new ReducedFileParser(new File(config.queries))
      this.KNNStructure = loadKNNStructure
    }

    case Ready => {
      println("status received: Structure is Ready")
      this.lshStructureReady = true
      // Start the test
      self ! StartPerformanceTest
    }

    case QueryResult(res) => {
      //println(res.length)
      this.candidateTotalSet+=res.length

      // last query sent
      val query = this.lastQuerySent

      // Get KNN result set
      val knnRes:Array[(Int, Float)] = this.KNNStructure.get(query.q._1).head

      // Compare result from KNN with result from LSH by sum of distances ratio
      // 1.0 is perfect result, > 1 is less perfect

      // Add result to be averaged later
      val sumKnnRes = knnRes.map(x => x._2).sum
      val sumQRes = res.map(x => x._2).sum
      this.recallBuffer += {
        if (sumKnnRes.equals(0) || sumQRes.equals(0)) {
          println("SHIT GOT REAL")
          0
        }
        else {
          sumQRes / sumKnnRes
        }
      }

      this.testProgress += 1.0

      // Print progress
      if(testProgress % (config.queriesSetSize / 100) == 0) {
        println("test "+(this.testsProgress+1)+" out of "+this.testCount+" : " + ((testProgress / config.queriesSetSize) * 100).toInt + "%")
      }

      // Was this the last query for this config?
      if(!queryParser.hasNext) {

        val avgRecall:Float = recallBuffer.sum/config.queriesSetSize
        val recallVariance = {
          var tmp = 0f
          for(r <- recallBuffer) {
            tmp+=(r-avgRecall)*(r-avgRecall)
          }
          tmp / recallBuffer.size
        }
        val recallStdDev = Math.sqrt(recallVariance)
        val avgHamming = hammingDists.sum / config.queriesSetSize

        var sb = new StringBuilder
        sb.append(config.dataSetSize+" ")
        sb.append(config.functions+" ")
        sb.append(config.knn+" ")
        sb.append(config.tables+" ")
        sb.append(config.range+" ")
        sb.append(config.queriesSetSize+" ")
        sb.append(avgRecall+" ")
        sb.append(recallStdDev+" ")
        sb.append(candidateTotalSet/config.queriesSetSize+" ")
        sb.append(config.measure+" ")
        sb.append(config.numOfDim+" ")
        sb.append(config.hashfunction+" ")
        sb.append(config.probingScheme+" ")
        sb.append(System.getProperty("line.separator"))

        // Write resulting set
        Files.write(Paths.get("data/logFile.log"), sb.toString.getBytes(), StandardOpenOption.APPEND);

        // RESET Counters
        this.candidateTotalSet = 0
        this.recallBuffer = ArrayBuffer.empty
        this.hammingDists = ArrayBuffer.empty

        this.testsProgress += 1
        println("Accuracy Test "+this.testsProgress.toInt+" out of " + this.testCount + " has finished")

        this.testProgress = 0.0

        // Start next test !
        if(this.configs.hasNext)
          self ! InitializeStructure
        else
          context.system.terminate()

      } else {
        // Go ahead to next query!
        this.lastQuerySent = Query(queryParser.next, config.range, config.probingScheme, config.measure, config.knn)
        lshStructure ! this.lastQuerySent
      }

    }
    case StartPerformanceTest => {
      println("Starting performance test, since tables are ready")
      // Run first accuracytest
      val q = Query(this.queryParser.next, config.range, config.probingScheme, config.measure, config.knn)
      this.lastQuerySent = q
      this.lshStructure ! this.lastQuerySent
    }
  }

  def loadKNNStructure = {
    println("Loading KNN Structure")
    val objReader = new ObjectInputStream(new FileInputStream(config.knnstructure))
    //KNNStructure
    val hashMap = objReader.readObject.asInstanceOf[mutable.HashMap[Int,Array[(Int,Float)]]]
    objReader.close
    hashMap
  }

}
