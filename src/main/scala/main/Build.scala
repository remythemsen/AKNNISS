package main

import java.io.File

/**
  * Created by remeeh on 9/26/16.
  */
case class Config(foo: Int = -1, out: File = new File("."), xyz: Boolean = false,
  libName: String = "", maxCount: Int = -1, verbose: Boolean = false, debug: Boolean = false,
  mode: String = "", files: Seq[File] = Seq(), keepalive: Boolean = false,
  jars: Seq[File] = Seq(), kwargs: Map[String,String] = Map())

object Build {
  def main(args:Array[String]) = {
    val parser = new scopt.OptionParser[Config]("scopt") {
      head("AKNNISS Build", "0.x")

      opt[File]('d', "data").required().valueName("<file>").
        action( (x, c) => c.copy(out = x) ).
        text("data to generate LSH Structure from")

      opt[Int]('k', "functions").action( (x, c) =>
        c.copy(foo = x) ).text("Number of Hashfunctions")

      opt[Int]('L', "tables").action( (x, c) =>
        c.copy(foo = x) ).text("Number of Hashtables\n")

      help("help").text("prints this usage text\n")

      note("AKNNISS - (Approximate K-Nearest Neighbor Image Similarity Search)\n created by Roxana, Remy, Chris, Fall 2016")

    }

    // parser.parse returns Option[C]
    parser.parse(args, Config()) match {
      case Some(config) =>
      // do stuff

      case None =>
      // arguments are bad, error message will have been displayed
    }
  }

}
