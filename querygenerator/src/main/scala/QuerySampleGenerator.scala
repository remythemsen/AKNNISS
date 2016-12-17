import java.io.{BufferedWriter, File, FileWriter}

import utils.IO.ReducedFileParser
import scala.util.Random

object QuerySampleGenerator {

  var outFile:File = _
  var bw:BufferedWriter = _
  val rnd = new Random(System.currentTimeMillis())

  def generateQuerySampleFile(fileName: String, outDir: String, sample: Int, inputSize: Int, chunks: Int): Unit={

    // hardcoded change with variable fileName
    val data = new ReducedFileParser(new File(fileName))

    var index = 0.0
    val percentile = inputSize / 100
    var sampleSize = 0

    var i = 0
    var subSampleSize = 0

    while(index < inputSize){
      if(index == i * (inputSize / chunks) && i <= chunks) {

        // After file has been filled
        println("Finished file " + i + " out of " + chunks + " with " + subSampleSize + " tuples")

        if(i != 0) { // if it is the first round, dont rename
          outFile.renameTo(new File(outDir + "queries-" +i+"-"+subSampleSize+".data"))
        }

        // Starting new file
        if(i < chunks) {
          outFile = new File(outDir + "queries-" + i)
          bw = new BufferedWriter(new FileWriter(outFile))
        }

        subSampleSize = 0
        i+=1
      }



      var randI = rnd.nextInt(600)
      val skipStep = randI % sample

      if(index % skipStep == 0){

        var randG = rnd.nextGaussian()
        if(randG > -2 || randG < 2){
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
          subSampleSize += 1
        }


      } else { data.next }
      // update progress by 1
      index += 1
      if(index % percentile == 0) {
        println(((index / inputSize) * 100).toInt.toString + "%")
      }
    }
    println("finished with "+ i +" files with a total of "+ sampleSize +" samples")
  }
}
