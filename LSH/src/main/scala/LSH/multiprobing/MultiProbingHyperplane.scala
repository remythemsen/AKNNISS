package LSH.multiprobing

import scala.collection.mutable.ArrayBuffer


class MultiProbingHyperplane(hashcode: Array[Int]){
  // hcode = the sequence of hashed values of the query vector
  private val hcode = hashcode

  // M = the number of hyperplanes
  private val M = hcode.length

  //generating 1-step, 2-step and 3-step probing buckets
  def generateProbes: ArrayBuffer[Array[Int]] = {
    val listBuckets = new ArrayBuffer[Array[Int]]()
    // adding the query itself
    listBuckets+=hcode
    // 1-step probing
    for(i <- 0 until M){
      var newCode = hcode
      newCode = hcode.updated(i, flipSign(hcode(i)))
      listBuckets += newCode
      // 2-step probing
       for(j <- i+1 until M){
         newCode = newCode.updated(j, flipSign(newCode(j)))
         listBuckets += newCode
//         for(k <- j+1 until M){
//           newCode = newCode.updated(k, flipSign(newCode(k)))
//           listBuckets += newCode
//         }
       }
    }
    listBuckets
  }
  private def flipSign(x:Int): Int ={
    if(x == 0) 1 else 0
  }

}
