package dataAnalysis

import scala.collection.mutable.ListBuffer

/**
  * Created by remeeh on 9/27/16.
  */
object ClusterAnalysis {
  def distAnalysis(data:List[(String, List[Double])]) = {
    // Convert to array for quick random access and size
    val tuples = data.toVector

    // Pick a random Point
    val rnd = scala.util.Random
    val rp = tuples(rnd.nextInt(tuples.length))

    var distances = ListBuffer[Double]()


    // Get distances from p to all other in P
    for(t <- tuples) {
      distances.insert(distTo(rp._2, t._2))
    }

    // Divide all distances into bins (decide bin size)



    // Plot resulting histogram
  }
  def distTo(p1:List[Double], p2:List[Double]) = {
    1
  }
}
