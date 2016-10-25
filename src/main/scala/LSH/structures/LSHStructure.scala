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
class LSHStructure(data:Parser, hf:() => HashFunction, L:Int) extends Serializable {

  // Set of Hash maps generated and populated by an LSH algorithm
  private var hashTables:ArrayBuffer[HashTable] = ArrayBuffer.empty

  /** [Constructor]
    * Builds the Structure by populating the L hash tables
    * each with the input set of vectors
  */

  // TODO Each actor creates a table

  for(i <- 0 until L) {
    val outI = i+1
    println(data.size)
    println(data.vLength)

    println(s"Building table $outI out of $L")
    // TODO Save each Table to disk and combine when this loop is finished

    val table = new HashTable(hf)
    var j:Double = 0
    val size = data.size.toDouble
    while(j < data.size) {
      j = j+1.0
      print("Table ")
      print(s"$outI out of $L is ")
      print(((j / size) * 100).toString.substring(0, 3))
      println("% done")


      var elem = data.next
      val reduced = (elem._1, DimensionalityReducer.getNewVector(elem._2, data.size, data.vLength))
      table+=(reduced)
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
      // TODO Move dimensionality reduction outside of query
      r <- h.query(DimensionalityReducer.getNewVector(v._2, data.size, data.vLength))
    } yield r

    val som = result.distinct.filter(x => dist.measure(x._2, v._2) < r).take(k)

    result.distinct.filter(x => dist.measure(x._2, v._2) < r).take(k)
  }
}
