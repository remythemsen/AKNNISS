package main

import LSH.hashFunctions._
import akka.actor._
import java.io.{File, FileOutputStream, ObjectOutputStream}
import scala.concurrent.ExecutionContext.Implicits.global

import IO.Parser

import scala.io.Source
import scala.concurrent.duration._
import akka.pattern._
import LSH.structures.HashTable
import akka.util.Timeout
import preProcessing.DimensionalityReducer

/**
  * Created by remeeh on 9/26/16.
  * This is the aggregating class combining the tables into a
  * LSHStructure, then saving said structure.
  *
  */
object Build extends App {
  override def main(args: Array[String]) = {
    val parser = getParser
    val res = parser.parse(args, ConfigBuild()) match {
      case Some(config) => {
        val hashFC: () => HashFunction = {
          if (config.hashFunction.equals("Hyperplane"))
            () => new Hyperplane(config.functions)
          else if (config.hashFunction.equals("Crosspolytope"))
            () => new CrossPolytope(config.functions)
          else {
            throw new Exception("Unknown Hash Function")
          }
        }

        // Time out for the ask operations
        implicit val timeout = Timeout(5.hour)

        val system = ActorSystem("LSHStructure Builder")
        val sb = system.actorOf(Props(StructureBuilder), "sb")
        // Grab structure (Its a future)
        val lshStructure = sb ? BuildStructure(config.tables, config.functions, config.data, 1000)
        lshStructure.foreach(println)

        system.terminate()

        // !!! SideEFFECT !!!
        val fis = new FileOutputStream(getOutFileName(config))
        val oos = new ObjectOutputStream(fis)
        oos.writeObject(lshStructure)
        oos.close

      }
      case None => {
        // arguments are bad, error message will have been displayed
        println("Invalid Arguments")
      }
    }
    def getOutFileName(config:ConfigBuild) = {
      config.outDir.concat("/")
        // constructing filename
        .concat(config.hashFunction)
        .concat("_")
        .concat(config.functions.toString)
        .concat("_")
        .concat(config.tables.toString)
        .concat(".lshstructure")
    }
    def getParser = new scopt.OptionParser[ConfigBuild]("build") {
      head("AKNNISS Build", "0.x")

      opt[File]('d', "data").required().valueName("<file>").
        action((x, c) => c.copy(data = x)).
        text("data to generate LSH Structure from")

      opt[String]('o', "outdir").required().valueName("<path>").
        action((x, c) => c.copy(outDir = x)).
        text("dir to store generated LSHStructure")

      opt[Int]('k', "functions").action((x, c) =>
        c.copy(functions = x)).text("Number of Hashfunctions")

      opt[Int]('L', "tables").action((x, c) =>
        c.copy(tables = x)).text("Number of Hashtables\n")

      opt[String]('h', "hashfunction").action((x, c) =>
        c.copy(hashFunction = x)).text("Hashfunction to use\n")

      help("help").text("prints this usage text\n\n")

      note("Approximate K-Nearest Neighbor Image Similarity Search\nCreated by Roxana, Remy and Chris, Fall 2016")
    }
  }
  case class ConfigBuild(data: File = new File("."), outDir: String = ".", functions:Int = 17, tables:Int = 4, hashFunction:String = "Hyperplane")
  case class PopulateTable(data:File, n:Int, vlength:Int, hashFunction: () => HashFunction)
  case class BuildStructure(L:Int, k:Int, file:File, n:Int)
  case object StructureBuilder extends Actor {
    private var number = 0
    def receive: Receive = {
      case BuildStructure(l, k, file, n) => {
        // spawn table builders
        for(i <- 0 until l) {
          context.actorOf(Props(TableBuilder), "tableBuilder"+number)
          number+=1
        }

        // TODO foreach table builder, call pop table(file)
        // aggregate tables into structure
      }
    }
  }
  case object TableBuilder extends Actor {
    def receive: Receive = {
      case PopulateTable(file, n, vl, hf) => {
        // TODO Divide work of populating an immutable table
        val data = new Parser(file)
        val table = new HashTable(hf)
        var i = 0
        val size = n
        val vLength = vl

        while(i < n) {
          i = i+1
          var elem = data.next
          val reduced = (elem._1, DimensionalityReducer.getNewVector(elem._2, size, vLength))
          table+=reduced
        }
        table
      }
    }
  }
}
