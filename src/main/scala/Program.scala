import java.io.{BufferedWriter, File, FileWriter}

import dataAnalysis.ClusterAnalysis
import LSH._

/**
  * Created by remeeh on 9/26/16.
  */

object Program {
  def main(args:Array[String]) = {

    // Getting from file
    val data = IO.Parser.parseInput(getClass.getResource("descriptors-mini.data").getPath)

    val lshs = Hyperplane.build(data, 1, 1)

    val result = lshs.query(data.head, 1, 90.0)

    result.foreach {
      println
    }

    // Outputting to file
    //IO.HTMLGenerator.outPut(data)


    // uncomment for distanalysis output
    //val f = ClusterAnalysis.distAnalysis(data, Cosine, 1, 100)
    //f.saveas("figure.png")

  }

}
