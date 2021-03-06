import java.io.File

import utils.IO.DisaFileParser
import utils.IO.ReducedFileParser

object QuerySampleGeneratorTest {

  def main(args: Array[String]): Unit = {
    println("Hello, world!")

    val queryParserSample = new ReducedFileParser(new File("querySampleFile2.data"))
    val arrayOfSamples = new Array[(Int,Array[Float])](queryParserSample.size)

    var i=0
    while(queryParserSample.hasNext){
      arrayOfSamples(i) = (queryParserSample.next)
      i+=1
    }

    for(i<-0 until arrayOfSamples.size){
      print(arrayOfSamples(i)._1+" : ")
      for(j<-0 until arrayOfSamples(i)._2.size){
        print(arrayOfSamples(i)._2(j)+" ")
      }
      println()
    }
  }

}
