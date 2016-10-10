package LSH

/**
  * Created by remeeh on 10/10/16.
  */
trait LSH {
  def hash(v:Vector[Double]) : Int
  def build(data:List[(String, Vector[Double])], k:Int, l:Int) : LSHStructure
}
