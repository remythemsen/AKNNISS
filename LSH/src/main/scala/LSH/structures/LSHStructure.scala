package LSH.structures

import LSH.tools.Distance

class LSHStructure(hts:IndexedSeq[HashTable]) extends Serializable {
  val hashTables = hts

  // Building lookup map
  private val items = this.hashTables.head.table.valuesIterator.flatten.toList
  val lookupMap = Map(items map { s => (s._1, s._2)} : _*)

  /**
    * Takes a query vector and finds neighbours withing range in the LSH Structure
    *
    * @param v Query vector
    * @param r Accepting neighbours within range
    * @return set of k near neighbours
    */

  def query(v:(String, Array[Float]), r:Double, dist:Distance) : IndexedSeq[(String, Array[Float])] = {
    val result = for {
      h <- hashTables
      r <- h.query(v._2)
    } yield r

    result.distinct.filter(x => dist.measure(x._2, v._2) < r)
  }

  def findVectorById(id:String):(String, Array[Float]) = {
    (id, lookupMap.get(id).head)
  }
}

