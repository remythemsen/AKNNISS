package LSH

import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/**
  * Created by remeeh on 9/26/16.
  */
object Hyperplane extends LSH {
  def hash(v: Vector[Double], randomV: Vector[Double]): Int = {
    if (Distance.dotProduct(v, randomV) > 0) 1 else 0
  }

  override def build(data: Stream[(String, Vector[Double])], k: Int, l: Int): LSHStructure = {
    // complexity O(l*V*k)

    val structure = new LSHStructure

    structure.hashMaps = ArrayBuffer.empty

    // Initializing
    for(hm <- 0 until l) {
      structure.hashMaps += new HashMap[String,Int]()
    }

    // TODO Rewrite to use immutable hashtables, building those functional style.
    // insert into H[i]
    for(hm <- structure.hashMaps) {// i is i'th hashtable
      // Initialize k random hyperplanes for H[i]
      var hs = new ArrayBuffer[Vector[Double]]
      for(m <- 0 until k) {
        hs += generateRandomV(4096, System.currentTimeMillis())
      }

      var key : ArrayBuffer[Char] = new ArrayBuffer[Char]()

      for(v <- data) {
        for(f <- 0 until k) { // f corresponds to key[f]
          key += hash(v._2, hs(f)).toChar
        }
        // Concat key, insert into H[i]
        hm += Tuple2(key.mkString, v._1.toInt)

        // reset
        key = new ArrayBuffer[Char]()
      }
    }
    structure
  }

  // TODO check the seq structure
  def generateRandomV(size: Int, seed: Long): Vector[Double] = {
    val buf = new ArrayBuffer[Double]
    val rnd = new Random(seed)
    for (i <- 0 until size)
      buf += (if (rnd.nextGaussian() < 0) -1 else 1)

    buf.toVector
  }
}
