/**
  * Created by remeeh on 9/27/16.
  * This object is meant to find precise k nearest neighbours and to calculate precision
  * on LSHStructures
  */

import LSH.{Distance, LSHStructure}

object KNN {
  def findKNearest(q:(String,Vector[Double]), k:Int, tuples:List[(String, Vector[Double])], distance:Distance) : List[(String, Double)] = {
    // Measure distances from q to each other tuple
    val distances = for {
      t <- tuples
    } yield (t._1, distance.measure(q._2, t._2))

    // Take out first k, return candidate-set
    distances.sortBy(_._2).take(k)
  }

  def precision(structure:LSHStructure) = {
    // Run queries on structure, then compare results with
    // results from KNN
  }


}
