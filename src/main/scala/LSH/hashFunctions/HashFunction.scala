package LSH.hashFunctions

/**
  * Created by remeeh on 10/15/16.
  */
@SerialVersionUID(100L)
abstract class HashFunction(k:Int) extends Serializable {
  def apply(v:Vector[Float]) : String
}
