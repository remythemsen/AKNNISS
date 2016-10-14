package LSH

// TODO Change out mutable hashmap in favor of immutable
import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer

/**
  * This Structure is the container for a LSH generated data structure
  * Used to run queries from.
  */

class LSHStructure {
  // Returns approximate k neighbors within range r of vector v.
  def query(v:(String, Vector[Double]), k:Int, r:Double) : List[String] = {
    List("Hello", "Lol")
  }
  // Set of Hash maps generated and populated by an LSH algorithm
  var hashMaps:ArrayBuffer[HashMap[String, Int]] = ArrayBuffer.empty


}
