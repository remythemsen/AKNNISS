package dataAnalysis

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

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
      distances.+=(distTo(rp._2, t._2))
    }

    // Divide all distances into bins (decide bin size)
    // Map to range 0.0 - 1.0 (affine transformation)
    // Get max
    val sortedDistances = distances.sorted
    val max = sortedDistances.last
    // Get min
    println(sortedDistances.apply(0))
    val min = sortedDistances.apply(0) // TODO: Find instead min of dataset
    // tranform into range 0.0 - 1.0
    val transformedSortedDistances = sortedDistances.map(x => transformIntoRange(x, min, max, 0.0, 1.0))


    // Plot resulting histogram
    val numberOfBuckets = 100;

    val buckets = ArrayBuffer[Int]()
    for( a <- 1 to numberOfBuckets ) {
      buckets+=(0)
    }

    for(x <- transformedSortedDistances) {
      println(x)
      val bucket = findBucket(x, numberOfBuckets)
      buckets.update(bucket, buckets(bucket) + 1)
    }



  }
  def findBucket(x:Double, n:Int) : Int = {
    Math.floor(x * (n)).toInt
  }
  def distTo(p1:List[Double], p2:List[Double]) = {
    val rnd = scala.util.Random
    rnd.nextDouble()
  }
  def transformIntoRange(x:Double, min:Double, max:Double, newMin:Double, newMax:Double) : Double = {
    (x-min)*(newMax-newMin/max-min)+newMin
  }
}
