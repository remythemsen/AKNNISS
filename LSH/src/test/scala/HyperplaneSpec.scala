import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}

import java.util
import LSH.hashFunctions.Hyperplane
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random

/**
  * Created by remeeh on 28-10-2016.
  */
class HyperplaneSpec extends FlatSpec with Matchers {
  "Hyperplane Function" should "produce the same value on same input" in {
    val f = () => new Random(System.currentTimeMillis)
    val hp = new Hyperplane(10, f, 4)
    val testVector = Array(0.0f, 5.2f, 1.3f, 3.6f)
    util.Arrays.hashCode(hp.apply(testVector)).equals(util.Arrays.hashCode(hp.apply(testVector))) should be (true)
  }
}
