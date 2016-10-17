package main

import java.io.File

/**
  * Created by remeeh on 9/26/16.
  */
object Build {
  def main(args:Array[String]) = {
    val parser = new scopt.OptionParser[Config]("build") {
      head("AKNNISS Build", "0.x")

      opt[File]('d', "data").required().valueName("<file>").
        action( (x, c) => c.copy(out = x) ).
        text("data to generate LSH Structure from")

      opt[File]('o', "outdir").required().valueName("<path>").
        action( (x, c) => c.copy(out = x) ).
        text("dir to store generated LSHStructure")

      opt[Int]('k', "functions").action( (x, c) =>
        c.copy(functions = x) ).text("Number of Hashfunctions")

      opt[Int]('L', "tables").action( (x, c) =>
        c.copy(tables = x) ).text("Number of Hashtables\n")

      help("help").text("prints this usage text\n\n")

      note("Approximate K-Nearest Neighbor Image Similarity Search\nCreated by Roxana, Remy and Chris, Fall 2016")

    }

    // parser.parse returns Option[C]
    parser.parse(args, Config()) match {
      case Some(config) =>
        // Run constructor with params
        // Save LSHStructure to file.
        // TODO: Add Error handling

      case None =>
      // arguments are bad, error message will have been displayed
    }
  }
}
case class Config(data: File = new File("."), out: File = new File("."), functions:Int = 17, tables:Int = 8)


