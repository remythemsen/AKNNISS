package LSH.structures
import IO.Parser
import LSH.hashFunctions.HashFunction
import preProcessing.DimensionalityReducer
import tools.Distance
import scala.collection.mutable.ArrayBuffer

/**
  * Structure of hashtables meant for querying k near neighbors of v
  * @param hf Hashfunction type used to map a vector v to an integer(key) k times
  * @param L Number of hashtables (Add more for higher prop of True Positives)
  *
  */

@SerialVersionUID(100L)
class LSHStructure(tables:ArrayBuffer[HashTable], hf:() => HashFunction, size:Int, vLength:Int) extends Serializable {

  // Set of Hash maps generated and populated by an LSH algorithm
  private val hashTables:ArrayBuffer[HashTable] = tables

  /** [Constructor]
    * Builds the Structure by populating the L hash tables
    * each with the input set of vectors
  */

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
      // TODO Move dimensionality reduction outside of query
      r <- h.query(DimensionalityReducer.getNewVector(v._2, size, vLength))
    } yield r

    result.distinct.filter(x => dist.measure(x._2, v._2) < r).take(k)
  }
}
