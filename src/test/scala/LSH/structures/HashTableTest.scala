package LSH.structures

import LSH.hashFunctions.Hyperplane
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by remeeh on 28-10-2016.
  */
class HashTableTest extends FlatSpec with Matchers {
  "HashTable" should "contain elem x after insert" in {
    val ht = new HashTable(() => new Hyperplane(10))
    val testElem = ("0000001234", Vector(0.0, 5.2, 1.3, 3.6, 2.4, 9.2, 2.3, 3.9, 0.3, 1.2, 1.1, 9.9, 0.0, 0.3013, 0.0))
    ht+=testElem
    val returnedElem = ht.query(testElem._2)
    returnedElem.head.equals(testElem) should be (true)
  }
}
