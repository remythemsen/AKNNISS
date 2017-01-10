package tools

import scala.util.Random
import org.scalatest._

/**
  * Created by remeeh on 07-11-2016.
  */
class DistanceSpec extends FlatSpec with Matchers {
  "Cosine Measure" should "have cosine distance of 0 when comparing two identical vectors" in {
    val rnd = new Random(System.currentTimeMillis())
    for(i <- 0 until 100) {
      var v1: Array[Float] = Array.fill(4096)(rnd.nextFloat * 2 - 1)
      var v2 = v1
      Cosine.measure(v1, v2) should be (0.0)
    }
  }

  "Cosine Measure" should "not measure higher than 1.0" in {
    val rnd = new Random(System.currentTimeMillis())

    // TODO try to fill vector with +- not only positives
    for(i <- 0 until 10000) {
      val v1 = Array.fill(220)(rnd.nextFloat() * 12000 - 6000)
      val v2 = Array.fill(220)(rnd.nextFloat * 12000 - 6000)
      (Cosine.measure(v1,v2) <= 1.0) should be (true)
    }
  }

}
