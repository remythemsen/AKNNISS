package LSH.hashFunctions

import scala.collection.mutable.ArrayBuffer


class MultiProbingHyperplane(hashcode: IndexedSeq[Int]){

  // hcode = the sequence of hashed values of the query vector
  val hcode = hashcode

  // M = the number of hyperplanes
  val M = hcode.size
  val listBuckets = new ArrayBuffer[IndexedSeq[Int]]()

  //generating 1-step, 2-step and 3-step probing buckets
  def generateProbes(): Unit = {
    for(i <- 0 until M){
      var newCode = hcode
      newCode = hcode.updated(i, flipSign(hcode(i)))
      listBuckets += newCode
      for(j <- i+1 until M){
        newCode = newCode.updated(j, flipSign(newCode(j)))
        listBuckets += newCode
        for(k <- j+1 until M){
          newCode = newCode.updated(k, flipSign(newCode(k)))
          listBuckets += newCode
        }
      }
    }
  }
  def flipSign(x:Int): Int ={
    if(x == 0) 1 else 0
  }

}
