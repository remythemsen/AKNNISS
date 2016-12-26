import java.io.{BufferedWriter, File, FileWriter}

import utils.IO.ReducedFileParser
import utils.tools.Distance

import scala.util.Random



object NormalizedVectors extends App {

  val inputFile = "25_direct_sample_queries.data"
  val inputDir = "data"
  val outputDir = "data"
  generateNormalizedFile(inputDir + "/" + inputFile,outputDir)

  def generateNormalizedFile(fileName: String, outDir: String): Unit={

    // hardcoded change with variable fileName
    val data = new ReducedFileParser(new File(fileName))

    var index = 0.0
    val percentile = data.size / 100
    var sampleSize = 0
    var outFile = new File(outDir+"/"+inputFile.substring(0, inputFile.length-5)+"-norm.data")
    var bw = new BufferedWriter(new FileWriter(outFile))

    while(data.hasNext){

      var v=data.next
      var tmpTuple = Distance.normalize(v._2)

      if (tmpTuple.size == 256) {
        // Write to file
        var sb = new StringBuilder
        sb.append(v._1)
        sb.append(" ")
        for (component <- tmpTuple) {
          sb.append(component + " ")
        }
        sb.append("\n")

        // Write resulting set
        bw.write(sb.toString())
        sampleSize += 1
      }

      // update progress by 1
      index += 1
      if(index % percentile == 0) {
        println(((index / data.size) * 100).toInt.toString + "%")
      }
    }
    bw.close()

    println("finished file with a total of "+ sampleSize +" samples")
  }
}




