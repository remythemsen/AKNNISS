package LSH.hashFunctions

/**
  * Created by remeeh on 10/15/16.
  */
abstract class HashFunction(k:Int) {
  def apply(v:Vector[Double]) : String
}
