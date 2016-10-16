package LSH.structures

import LSH.hashFunctions.HashFunction
import scala.collection.mutable

/**
  * Created by remeeh on 10/15/16.
  */

class HashTable(f:() => HashFunction) {
  // internal Mutable HashMap
  val table = new mutable.HashMap[String, (String, Vector[Double])]()
  // internal Hash function
  val hf = f()

  /**
    * Insert vector
    * @param v vector to be inserted into internal hashmap
    */
  def +=(v:(String, Vector[Double])) : Unit = {
    table += (hf(v._2) -> v)
  }

  /**
    * @param v a query point
    * @return a list of vectors with same key as v
    */
  def query(v:Vector[Double]) : Stream[(String, Vector[Double])] = {
    table.filterKeys((x) => x.equals(hf(v))).map((x) => x._2).toStream
  }

}
