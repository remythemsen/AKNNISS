import java.io.{BufferedWriter, File, FileWriter}

import LSH.Cosine
import dataAnalysis.ClusterAnalysis

/**
  * Created by remeeh on 9/26/16.
  */

object Program {
  def main(args:Array[String]) = {

    // Getting from file
    //val data = IO.Parser.parseInput(getClass.getResource("descriptors-decaf-random-sample.data").getPath)

    // Outputting to file
    //IO.HTMLGenerator.outPut(data)


    // uncomment for distanalysis output
    //val f = ClusterAnalysis.distAnalysis(data, Cosine, 1, 100)
    //f.saveas("figure.png")

  }

}
