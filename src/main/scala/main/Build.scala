package main

import java.io.{File, FileOutputStream, ObjectOutputStream}
import IO.Parser
import LSH.hashFunctions.{CrossPolytope, HashFunction, Hyperplane}
import LSH.structures.HashTable
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import preProcessing.DimensionalityReducer
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Build {

  // TODO Deal with cases of success and failures
  implicit val timeout = Timeout(3.hours)

  case class BuildTables(timeOut:Timeout)
  case class ConfigBuild(data: File = new File("."), outDir: String = ".", functions:Int = 17, tables:Int = 4, hashFunction:String = "Hyperplane")

  def main(args: Array[String]) = {
    val parser = new scopt.OptionParser[ConfigBuild]("build") {
      head("AKNNISS Build", "0.x")

      opt[File]('d', "data").required().valueName("<file>").
        action( (x, c) => c.copy(data = x) ).
        text("data to generate LSH Structure from")

      opt[String]('o', "outdir").required().valueName("<path>").
        action( (x, c) => c.copy(outDir = x) ).
        text("dir to store generated LSHStructure")

      opt[Int]('k', "functions").action( (x, c) =>
        c.copy(functions = x) ).text("Number of Hashfunctions")

      opt[Int]('L', "tables").action( (x, c) =>
        c.copy(tables = x) ).text("Number of Hashtables\n")

      opt[String]('h', "hashfunction").action( (x, c) =>
        c.copy(hashFunction = x) ).text("Hashfunction to use\n")

      help("help").text("prints this usage text\n\n")

      note("Approximate K-Nearest Neighbor Image Similarity Search\nCreated by Roxana, Remy and Chris, Fall 2016")

    }

    // parser.parse returns Option[C]
    parser.parse(args, ConfigBuild()) match {
      case Some(config) =>
        val hashFC:() => HashFunction = {
          if (config.hashFunction.equals("Hyperplane"))
            () => new Hyperplane(config.functions)
          else if (config.hashFunction.equals("Crosspolytope"))
            () => new CrossPolytope(config.functions)
          else {
            throw new Exception("Unknown Hash Function")
          }
        }
        // Save LSHStructure to file.
        val dir:String = config.outDir.concat("/")
          // constructing filename
          .concat(config.hashFunction)
          .concat("_")
          .concat(config.functions.toString)
          .concat("_")
          .concat(config.tables.toString)
          .concat(".lshstructure")


        val system = ActorSystem("LSHStructureBuilder")
        val sb = system.actorOf(Props(new StructureBuilder(config.tables, config.functions, hashFC, config.data)), "sb")
        val lshStructure = Await.result(sb ? BuildTables(Timeout(5.hours)), Timeout(5.hours).duration)
        system.terminate()

        val fis = new FileOutputStream(dir.toString)
        val oos = new ObjectOutputStream(fis)
        oos.writeObject(lshStructure)
        oos.close

      case None =>
        // arguments are bad, error message will have been displayed
        println("Invalid Arguments")
    }
  }

  class StructureBuilder(l:Int, k:Int, hf:()=>HashFunction, data:File) extends Actor {
    case object BuildTable
    def receive = {
      case BuildTables(timeOut) => {
        implicit val timeout = timeOut
        val futs = new ArrayBuffer[Future[Any]]()
        for(i <- 0 until l) {
          val c = context.actorOf(Props(new TableBuilder(k, hf, data)), "c"+i)
          futs += c ? BuildTable
        }
        val res = Await.result(Future.sequence(futs), timeout.duration)
        sender ! res
      }
    }

    class TableBuilder(k:Int, hf: () => HashFunction, data:File) extends Actor {
      val parser = new Parser(data)
      def receive = {
        case BuildTable => {
          println("Starting "+self.path)
          val table = new HashTable(hf)
          var j:Double = 0.0
          val size = parser.size.toDouble
          while(j < size) {
            j = j+1.0
            println("Table "+self.toString()+" is " + ((j / size) * 100) + "% done")

            var elem = parser.next
            val reduced = (elem._1, DimensionalityReducer.getNewVector(elem._2, parser.size, parser.vLength))
            table+=(reduced)
          }
          println("Table "+self.path+" is done")
          sender ! table
        }
      }
    }
  }
}
