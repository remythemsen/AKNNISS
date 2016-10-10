package LSH

/**
  * Created by remeeh on 9/26/16.
  */
class CrossPolytope extends LSH {
  override def hash(v: Vector[Double]): Int = ???

  override def build(data: List[(String, Vector[Double])], k: Int, l: Int): LSHStructure = ???
}
