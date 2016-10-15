package dataAnalysis
import LSH._
import breeze.plot._
import tools.Distance

/**
  * Created by remeeh on 9/27/16.
  */
object ClusterAnalysis {
  def distAnalysis(data:Stream[(String, Vector[Double])], dist:Distance, k:Int, b:Int) : Figure = {

    // Plot resulting histogram
    val f = Figure()
    val p = f.subplot(0)
    p.ylabel = "# of Points"
    p.xlabel = "Normalized Distance from P"

    @annotation.tailrec
    def loop(i:Int = 0) : Unit = {
      if(i != k) {
        p+= hist(getDistancesFromRandomPoint(data, dist),b)
        loop(i+1)
      }
    }
    loop(0)

    // Return figure object
    f
  }

  def normalize(x:Double, min:Double, max:Double) = (x-min)/(max-min) // change to std normalization

  def getDistancesFromRandomPoint(tuples:Stream[(String, Vector[Double])], dist:Distance) : Vector[Double] = {
    // TODO Efficiently Grab a Random "Chunk" of size 600-ish
    val rnd = scala.util.Random

    // TODO Select Random Tuple from chunk
    //val rp = tuples(rnd.nextInt(tuples.length))
    val rp = tuples(5)

    // Compute distances from rp to rest of chunk
    // Map to range 0.0 - 1.0 (affine transformation)
    val distances = for {
      t <- tuples
    } yield (dist.measure(rp._2, t._2))

    val sortedDs = distances.toVector.sorted.tail

    // normalize
    for {
      d <- sortedDs
    } yield (normalize(d, sortedDs.head, sortedDs(sortedDs.size-1)))
  }
}
