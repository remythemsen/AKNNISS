package LSH.structures

import LSH.hashFunctions.HashFunction
import tools.Distance

import scala.collection.mutable.ArrayBuffer

/**
  * Structure of hashtables meant for querying k near neighbors of v
  * @param hf Hashfunction type used to map a vector v to an integer(key) k times
  * @param L Number of hashtables (Add more for higher prop of True Positives)
  *
  */

class LSHStructure(data:Stream[(String, Vector[Double])], hf:() => HashFunction, L:Int) {

  // Set of Hash maps generated and populated by an LSH algorithm
  private var hashTables:ArrayBuffer[HashTable] = ArrayBuffer.empty

  /** [Constructor]
    * Builds the Structure by populating the L hash tables
    * each with the input set of vectors
  */

  for(i <- 0 until L) {
    val table = new HashTable(hf)
    for(v <- data) {
      table+=v
    }
    hashTables+=table
  }

  /**
    * Takes a query vector and finds k near neighbours in the LSH Structure
    * @param v Query vector
    * @param k Amount of neighbours
    * @param r Accepting neighbours within range
    * @return set of k near neighbours
  */

  def query(v:(String, Vector[Double]), k:Int, r:Double, dist:Distance) : ArrayBuffer[(String, Vector[Double])] = {
    val result = for {
      h <- hashTables
      r <- h.query(v._2)
    } yield r

    result.distinct.filter(x => dist.measure(x._2, v._2) <= r).take(k)
  }
}
