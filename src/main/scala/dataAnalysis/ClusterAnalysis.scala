package dataAnalysis
import LSH._
import breeze.plot._

/**
  * Created by remeeh on 9/27/16.
  */
object ClusterAnalysis {
  def distAnalysis(data:List[(String, Vector[Double])], dist:Distance) = {
    // Convert to vector for quick random access and size
    val tuples = data.toVector


    // Plot resulting histogram
    val f = Figure()
    val p = f.subplot(0)
    for(a <- 0 until 16) {
      p += hist(getDistancesFromRandomPoint(tuples, dist),100)
    }

    p.ylabel = "# of Points"
    p.xlabel = "Normalized Distance from P"
    f.saveas("lines.png")

  }
  def normalize(x:Double, min:Double, max:Double) = (x-min)/(max-min) // change to std normalization
  def getDistancesFromRandomPoint(tuples:Vector[(String, Vector[Double])], dist:Distance) : Vector[Double] = {
    // Pick a random Point
    val rnd = scala.util.Random
    //val rp = tuples(rnd.nextInt(tuples.length))
    val rp = tuples(5)

    // Compute distances from rp to rest
    // Map to range 0.0 - 1.0 (affine transformation)
    val distances = for {
      t <- tuples
    } yield (dist.measure(rp._2, t._2))

    val sortedDs = distances.sorted.tail // skip head since that will be q point

    // normalize
    for {
      d <- sortedDs
    } yield (normalize(d, sortedDs.head, sortedDs(sortedDs.size-1)))
  }
}
