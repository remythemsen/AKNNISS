//package main
//import java.io._
//
//import preProcessing.DimensionalityReducer
//
//import scala.io.Source
//
//object Preprocess {
//  def main(args:Array[String]) = {
//    val parser = new scopt.OptionParser[ConfigPreprocessing]("Dimensional reducer") {
//      head("AKNNISS Preprocessor Dimensionality Reducer", "0.x")
//
//      opt[File]('d', "data").required().valueName("<file>").
//        action( (x, c) => c.copy(data = x) ).
//        text("File to be preprocessed")
//
//      opt[String]('o', "outdir").required().valueName("<path>").
//        action( (x, c) => c.copy(outDir = x) ).
//        text("dir to store preprocessed file")
//
//      opt[Int]('n', "size").action( (x, c) =>
//        c.copy(n = x) ).text("number of tuples in data file")
//
//      opt[Int]('v', "vlength").action( (x, c) =>
//        c.copy(vlength = x) ).text("number of components in each vector\n")
//
//      help("help").text("prints this usage text\n\n")
//
//      note("Approximate K-Nearest Neighbor Image Similarity Search\nCreated by Roxana, Remy and Chris, Fall 2016")
//
//    }
//
//    // parser.parse returns Option[C]
//    parser.parse(args, ConfigPreprocessing()) match {
//      case Some(config) =>
//
//        // Reducing and saving
//        val input = new BufferedInputStream( new FileInputStream( config.data ) )
//
//        // Save LSHStructure to file.
//        val dir:String = config.outDir.concat("/")
//          // constructing filename
//          .concat(config.data.getName.substring(0,config.data.getName.length-5))
//          .concat("-reduced.data")
//
//        val output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dir.toString)))
//        val input2 = Source.fromFile(config.data.getAbsoluteFile).getLines()
//
//        var j = 0.0
//        val size = config.n.toDouble
//
//        // TODO This can be made much faster using buffer
//        Iterator
//          .continually (input2.next())
//          .takeWhile (_ => input2.hasNext)
//          .map(x => {
//            // case id line
//            if(x.startsWith("#")) x
//            else {
//              val sb : StringBuilder = new StringBuilder
//              val v = x.toString.split(" ").map(x=>x.toDouble).toVector
//              val newV = DimensionalityReducer.getNewVector(v, config.n, config.vlength)
//              for(d <- newV) {
//                sb ++= d.toString
//                sb ++= " "
//              }
//              sb.toString
//            }
//            // case vector line
//          })//(DimensionalityReducer.getNewVector(Vector(x.toDouble)).toString))
//          .foreach((x) => {
//            j+=1.0
//            println((((j/2) / size) * 100).toString.substring(0, 3)+"%")
//            output.write(x + "\n")
//          })
//
//      case None =>
//        // arguments are bad, error message will have been displayed
//        println("Invalid Arguments")
//    }
//  }
//}
//case class ConfigPreprocessing(data: File = new File("."), outDir: String = ".", n:Int = 0, vlength:Int = 4096)
