package dataAnalysis

import LSH.Distance
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/**
  * Created by remeeh on 9/27/16.
  */
object ClusterAnalysis {
  def distAnalysis(data:List[(String, Vector[Double])]) = {
    // Convert to array for quick random access and size
    val tuples = data.toVector

    // Pick a random Point
    val rnd = scala.util.Random
    val rp = tuples(rnd.nextInt(tuples.length))

    // Compute distances from rp to rest
    // Map to range 0.0 - 1.0 (affine transformation)
    val distances = for {
      t <- tuples
    } yield (Distance.euclideanDistance(rp._2, t._2))

    val sortedDs = distances.sorted.tail // skip head since that will be q point

    // normalize
    val nDistances = for {
      d <- sortedDs
    } yield (transformIntoRange(d, sortedDs.head, sortedDs(sortedDs.size-1), 0.0, 1.0))

    // divide into buckets and count
    val numberOfBuckets = 100;

    val buckets = ArrayBuffer[Int]()
    for(a <- 0 until numberOfBuckets) {
      buckets+=(0)
    }

    for(x <- nDistances) {
      val bucket = findBucket(x, numberOfBuckets-1)
      buckets.update(bucket, buckets(bucket) + 1)
    }

    // Plot resulting histogram


    for (x <- buckets) {
      println(x)
    }
  }

/*
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
    for( a <- 0 to numberOfBuckets-1 ) {
      buckets+=(0)
    }

    for(x <- transformedSortedDistances) {
      println(x)
      val bucket = findBucket(x, numberOfBuckets)
      buckets.update(bucket, buckets(bucket) + 1)
    }



  }
  */
  def findBucket(x:Double, n:Int) : Int = {
    Math.floor(x * (n)).toInt
  }
  def transformIntoRange(x:Double, min:Double, max:Double, newMin:Double, newMax:Double) : Double = {
    (x-min)/(max-min) // change to std normalization
  }
}
