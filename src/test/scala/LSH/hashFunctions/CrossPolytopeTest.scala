package LSH.hashFunctions

import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by remeeh on 10/29/16.
  */
class CrossPolytopeTest extends FlatSpec with Matchers {
  "CrossPolytope APPLY Function" should "produce the same value on same input" in {
    val cp = new CrossPolytope(30)
    val testVector = Vector(0.0, 5.2, 1.3, 3.6, 9.5, 10.0, 11.2, 9.1, 2.3)
    val t:String = cp(testVector)
    println(t)
    cp.apply(testVector).equals(cp.apply(testVector)) should be(true)
  }
}
