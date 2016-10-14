package LSH

/**
  * Created by remeeh on 10/10/16.
  */
trait LSH {
  def build(data:Stream[(String, Vector[Double])], k:Int, l:Int) : LSHStructure
}
