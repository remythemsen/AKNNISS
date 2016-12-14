import java.io.{BufferedWriter, File, FileWriter}

import utils.IO.ReducedFileParser

import scala.io.Source

object QuerySampleGenerator {

  def generateQuerySampleFile(fileName:String, newFileName:String, sample:Int, inputSize:Int): Unit={

    // hardcoded change with variable fileName
    val data = new ReducedFileParser(new File(fileName))

    val sampleFile = new File(newFileName)
    val bw = new BufferedWriter(new FileWriter(sampleFile))
    var index = 0.0
    val percentile = inputSize / 100
    var sampleSize = 0

    while(index < inputSize){
      if(index % sample == 0 && index / sample > 0){
        var tmpTuple = data.next

        // Write to file
        var sb = new StringBuilder
        sb.append(tmpTuple._1)
        sb.append(" ")
        for (component <- tmpTuple._2) {
          sb.append(component + " ")
        }
        sb.append("\n")

        // Write resulting set
        bw.write(sb.toString())
        sampleSize += 1

      } else { data.next }
      // update progress by 1
      index += 1
      if(index % percentile == 0) {
        println(((index / inputSize) * 100).toInt.toString + "%")
      }
    }

    println("Finished with "+ sampleSize+" tuples")
  }
}
