import java.io.{BufferedWriter, File, FileWriter}

import IO.DisaFileParser

object QuerySampleGenerator {

  def generateQuerySampleFile(fileName:String,newFileName:String,sample:Int):Unit={

    //hardcoded change with variable filenName
    //val queryParserSample = new DisaFileParser(new File("C:/Users/chm/Desktop/IdeaProjects/AKNNISS/descriptors-mini.data"))
    val queryParserSample = new DisaFileParser(new File(fileName))
    val arrayOfSamples=new Array[(Int,Array[Float])](queryParserSample.size/sample)
    var arrayIndex=0
    var index=1;

    //println(queryParserSample.size)
    while(index <= queryParserSample.size){
      if(index % sample==0 && index/sample>0){
        arrayOfSamples(arrayIndex)= queryParserSample.next
        arrayIndex+=1
      }
      else{queryParserSample.next}
      index+=1
    }

    //hardcoded fileName
    //val sampleFile = new File("querySampleFile.data")
    val sampleFile = new File(newFileName)
    val bw = new BufferedWriter(new FileWriter(sampleFile))
    var j = 0.0
    val n = arrayOfSamples.size
    var progress = 0
    var i=0
    while (progress != n) {

      var temp = arrayOfSamples(i)
      var sb = new StringBuffer(456)
      sb.append(temp._1)
      sb.append(" ")
      for (component <- temp._2) {
        sb.append(component + " ")
      }
      sb.append("\n")

      // Write resulting set
      bw.write(sb.toString())
      i+=1
      j+=1.0
      progress += 1
      if (j % 100 == 0) {
        println(((j / n) * 100).toInt.toString + "%")
      }
    }
    println("Finished with "+n+" tuples")
  }
}
