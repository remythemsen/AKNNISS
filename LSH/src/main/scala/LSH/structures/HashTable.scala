package LSH.structures

import java.util

import LSH.hashFunctions.{CrossPolytope, HashFunction}
import LSH.multiprobing.{MultiProbingCrossPolytope, MultiProbingHyperplane}
import utils.tools.Cosine

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Created by remeeh on 10/15/16.
  */
class HashTable(f:() => HashFunction) {
  // internal Mutable HashMap
  val table = new mutable.HashMap[Int, ArrayBuffer[(Int, Array[Float])]]()

  // internal Hash function
  val hf = f()

  /**
    * Insert vector
    * @param v vector to be inserted into internal hashmap
    */
  def +=(v:(Int, Array[Float])) : Unit = {
    val key = util.Arrays.hashCode(hf(v._2))
    val value = {
      if(this.table.contains(key)) this.table(key)++ArrayBuffer(v)
      else ArrayBuffer(v)
    }
    this.table += (key -> value)
  }

  /**
    * @param v a query point
    * @return a list of vectors with same key as v
    */
  def query(v:Array[Float]) : ArrayBuffer[(Int, Array[Float])] = {
    val key = hf(v)
    this.table(util.Arrays.hashCode(key))
  }

  def mpQuery(q:Array[Float], range:Double, probingScheme:String) : ArrayBuffer[(Int, Array[Float])] = {
    // keys of buckets to be probed
    var bucketsToBeProbed = new ArrayBuffer[Int]

    probingScheme match {
      case "Hyperplane" => {
        val p = new MultiProbingHyperplane(hf(q))
        bucketsToBeProbed = p.generateProbes().map(x => util.Arrays.hashCode(x))
      }
      case "Crosspolytope" => {
        // T = 3
        val rotations = hf.asInstanceOf[CrossPolytope].rotations
        val arrayOfMaxIndices = hf.asInstanceOf[CrossPolytope].arrayOfMaxIndices
        val p = new MultiProbingCrossPolytope(rotations, arrayOfMaxIndices, 3)
        bucketsToBeProbed = p.generateProbes().map(x => util.Arrays.hashCode(x))
      }
      case "None" => {
        bucketsToBeProbed = ArrayBuffer(util.Arrays.hashCode(hf(q)))
      }
      case _ => {
        throw new Exception("Unknown Probing scheme")
      }
    }

    var candidates : ArrayBuffer[(Int, Array[Float])] = for {
      b <- bucketsToBeProbed
      c <- this.table(b)
    } yield c

    candidates
  }

}

