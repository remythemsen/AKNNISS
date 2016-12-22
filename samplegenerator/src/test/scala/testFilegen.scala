import java.io.{BufferedWriter, File, FileWriter}

import utils.IO.ReducedFileParser

import scala.util.Random



object testFilegen {
  def main(args: Array[String]): Unit = {

      generateSampleFile("data/descriptors-decaf-500k.data","data/")
  }

  def generateSampleFile(fileName: String, outDir: String): Unit={

    // hardcoded change with variable fileName
    val data = new ReducedFileParser(new File(fileName))

    var index = 0.0
    val percentile = data.size / 100
    var sampleSize = 0
    var outFile = new File(outDir+"descriptors-decaf-500k-fixed.data")
    var bw = new BufferedWriter(new FileWriter(outFile))


    while(data.hasNext){

      var tmpTuple = data.next

      if (tmpTuple._2.size == 256) {
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




