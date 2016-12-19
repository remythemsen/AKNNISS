import java.io._
import java.nio.file.{Files, Paths, StandardOpenOption}
import LSH.structures.LSHStructure
import utils.tools.actorMessages._
import tools.status._
import akka.actor._
import utils.IO.ReducedFileParser
import utils.tools.{Cosine, Distance, Euclidean}
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
  var performanceFile=new File("data/performanceFile.txt")





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
        config.tables,
        config.functions,
        config.numOfDim,
        rnd.nextLong(),
        config.buildFromFile
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
      val recall = knnRes.map(x => x._2).sum / res.map(x=>x._2).sum

      // Add result to be averaged later
      this.recallBuffer += recall

      this.testProgress += 1.0

      // Print progress
      if(testProgress % (config.queriesSetSize / 100) == 0) {
        println("test "+(this.testsProgress+1)+" out of "+this.testCount+" : " + ((testProgress / config.queriesSetSize) * 100).toInt + "%")
      }

      // Was this the last query for this config?
      if(!queryParser.hasNext) {
        // Get average recalls and write line to log file
        var mean=0.0f
        val standardDev={
          for(i<-0 until recallBuffer.size){
            mean+=recallBuffer(i)
          }
          mean=mean/recallBuffer.size
          var variance=0.0
          for(i<-0 until recallBuffer.size){
            variance+=((recallBuffer(i)-mean)*(recallBuffer(i)-mean))
          }
          variance+=variance/recallBuffer.size
          Math.sqrt(variance)
        }
        val avgRecall= recall/config.queriesSetSize

        var sb = new StringBuilder
        sb.append(config.dataSetSize+","+config.functions+","+ config.knn+","+config.tables+","+config.range+","+config.queriesSetSize+
          ","+avgRecall+","+standardDev+","+ (candidateTotalSet/config.queriesSetSize) +","+config.measure+","+config.numOfDim+","+config.hashfunction)
        sb.append(System.getProperty("line.separator"))

        // Write resulting set
        Files.write(Paths.get("data/logFile.log"), sb.toString.getBytes(), StandardOpenOption.APPEND);

        this.candidateTotalSet = 0

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
        this.lastQuerySent = Query(queryParser.next, config.range, config.probingScheme, config.measure)
        lshStructure ! this.lastQuerySent
      }

    }
    case StartPerformanceTest => {
      println("Starting performance test, since tables are ready")
      var queryTimeBuffer=new ArrayBuffer[Double]()
      var sumOfResults=0.0
      while(queryParser.hasNext) {
        val startTime=System.currentTimeMillis()
        val q = Query(this.queryParser.next, config.range, config.probingScheme, config.measure)
        val endTime=System.currentTimeMillis()
        queryTimeBuffer+=(endTime-startTime) * 0.001
        sumOfResults+=(endTime-startTime) * 0.001
        this.lastQuerySent = q
      }
      val averageQueryTime = sumOfResults/queryTimeBuffer.size

      this.lshStructure ! this.lastQuerySent

      var sb1 = new StringBuilder
      for (line <- Source.fromFile("data/performanceFile.txt").getLines()) {
        sb1.append(line)
        sb1.append(System.getProperty("line.separator"));
      }
      sb1.append(config.dataSetSize+","+config.functions+","+config.tables+","+config.range+","+","+config.queriesSetSize+
        "," + averageQueryTime + "," + candidateTotalSet +","+config.measure+","+config.numOfDim+","+config.hashfunction)
      sb1.append(System.getProperty("line.separator"));

      // Write resulting set
      Files.write(Paths.get("data/performanceFile.txt"), sb1.toString.getBytes(), StandardOpenOption.APPEND);

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
