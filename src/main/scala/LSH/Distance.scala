package LSH

/**
  * Created by remeeh on 9/27/16.
  */
trait Distance {
  def distTo(p1:List[Double], p2:List[Double]) : Double
}


