/**
  * Created by remeeh on 9/27/16.
  */
import LSH.Distance

object KNN {
  def findKNearest(q:(String,Vector[Double]), k:Int, tuples:List[(String, Vector[Double])]) : List[(String, Double)] = {
    // Measure distances from q to each other tuple
    val distances = for {
      t <- tuples
    } yield (t._1, Distance.euclideanDistance(q._2, t._2))

    // Take out first k, return candidate-set
    distances.sortBy(_._2).take(k)
  }

  def precision() = {
    // Run LSH m times with random q points, return resultset
    // Hamming distance compare result from knearest, and result from LSH
    //
  }


}
