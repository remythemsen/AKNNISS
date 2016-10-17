package LSH.structures

import LSH.hashFunctions.HashFunction
import scala.collection.mutable

/**
  * Created by remeeh on 10/15/16.
  */

class HashTable(f:() => HashFunction) {
  // internal Mutable HashMap
  val table = new mutable.HashMap[String, List[(String, Vector[Double])]]()
  // internal Hash function
  val hf = f()

  /**
    * Insert vector
    * @param v vector to be inserted into internal hashmap
    */
  def +=(v:(String, Vector[Double])) : Unit = {
    val key = hf(v._2)
    val value = {
      if(table.contains(key)) table(key)++List(v)
      else List(v)
    }
    table += (key -> value)
  }

  /**
    * @param v a query point
    * @return a list of vectors with same key as v
    */
  def query(v:Vector[Double]) : Stream[(String, Vector[Double])] = {
    table(hf(v)).toStream
  }

}