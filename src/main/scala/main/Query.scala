package main
import java.io._

import IO.{HTMLGenerator, Parser}
import LSH.structures._
import tools.{Cosine, Euclidean}

import scala.collection.mutable.ArrayBuffer

object Query {
  def main(args:Array[String]) = {
    val parser = new scopt.OptionParser[Config]("query") {
      head("AKNNISS Query", "0.x")

      opt[File]('s', "structure").required().valueName("<file>").
        action( (x, c) => c.copy(structure = x) ).
        text("LSH Structure to run queries on")

      opt[File]('q', "queries").required().valueName("<file>").
        action( (x, c) => c.copy(queries = x) ).
        text("Set of queries to be run on LSHStructure")

      opt[String]('o', "outdir").required().valueName("<path>").
        action( (x, c) => c.copy(outDir = x) ).
        text("dir to store generated result")

      opt[Int]('k', "neighbours").action( (x, c) =>
        c.copy(neighbours = x) ).text("Max number of Near Neighbours")

      opt[Double]('r', "range").action( (x, c) =>
        c.copy(range = x) ).text("Maximum range a neighbour can be from q")

      opt[String]('d', "distance").action( (x, c) =>
        c.copy(distance = x) ).text("Distance measure to use")

      opt[String]('f', "outformat").
        action( (x, c) => c.copy(outFormat = x) ).
        text("format of output (default HTML)")

      opt[String]('p', "probetype").
        action( (x, c) => c.copy(probeType = x) ).
        text("type of probing")

      help("help").text("prints this usage text\n\n")
      note("Approximate K-Nearest Neighbor Image Similarity Search\nCreated by Roxana, Remy and Chris, Fall 2016")
    }
    // parser.parse returns Option[C]
    parser.parse(args, Config()) match {
      case Some(config) =>
        // Load LSHStructure
        val fip = new FileInputStream(config.structure)
        //val ois = new ObjectInputStream(fip)
        val ois = new ObjectInputStream(new FileInputStream(config.structure)) {
          override def resolveClass(desc: java.io.ObjectStreamClass): Class[_] = {
            try { Class.forName(desc.getName, false, getClass.getClassLoader) }
            catch { case ex: ClassNotFoundException => super.resolveClass(desc) }
          }
        }
        val lshs:LSHStructure = ois.readObject.asInstanceOf[LSHStructure]
        ois.close()

        // Load Queries
        print(config.queries.getAbsolutePath)
        println(" loaded!")
        val parser = new Parser(config.queries)
        var i = 0
        var queryPoints = new ArrayBuffer[(String, Vector[Double])](parser.size)
        while(i < parser.size) {
          i += 1
          queryPoints += parser.next
        }

        // Run i queries on it
        // TODO Autodetect class
        val distance = {
          if(config.distance.equals("Cosine")) {
            Cosine
          } else if (config.distance.equals("Euclidean")) {
            Euclidean
          } else {
            throw new Exception("Distance Not Known")
          }
        }


        val res = for {
          q <- queryPoints
          r <- lshs.query(q, config.neighbours, config.range, distance)
        } yield r
        // TODO check for format
        // Send result to be converted to right format
        // TODO Use writer class instead
        // TODO make loop for each query point
        val output = HTMLGenerator.outPut(res, queryPoints.head, config.outDir
          .concat("/")
          // TODO Implement LSHS Type
          .concat("lshs-type")
          .concat("_")
          // TODO Implement LSHS ID
          .concat("lshs-id")
          .concat("_")
          .concat(config.neighbours.toString)
          .concat("_")
          .concat(config.range.toString)
          .concat("_")
          .concat(config.distance)
          // TODO check for output type, and put into appropriate folder
          .concat(".html"))


      case None =>
        // arguments are bad, error message will have been displayed
        println("Invalid Arguments")
    }
  }
}
case class Config(structure: File = new File("."), queries:File = new File("."), neighbours:Int = 10, range:Double = 15.0, distance:String = "Cosine", outDir: String = ".", outFormat:String = "HTML", probeType:String="none")
