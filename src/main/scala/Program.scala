import java.io.{BufferedWriter, File, FileWriter}

import dataAnalysis.ClusterAnalysis
import LSH.structures.LSHStructure
import LSH.hashFunctions.Hyperplane

/**
  * Created by remeeh on 9/26/16.
  */

object Program {
  def main(args:Array[String]) = {

    // Getting from file
    val data = IO.Parser.parseInput(getClass.getResource("descriptors-mini.data").getPath)
    val lshs = new LSHStructure(data, () => new Hyperplane(5), 4)
    val result = lshs.query(data.head, 7, 90.0)

    // Outputting to file
    IO.HTMLGenerator.outPut(result)


    // uncomment for distanalysis output
    //val f = ClusterAnalysis.distAnalysis(data, Cosine, 1, 100)
    //f.saveas("figure.png")

  }

}
