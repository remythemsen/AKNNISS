package LSH.hashFunctions
import java.util.Arrays

import scala.collection.mutable
import scala.collection.mutable.PriorityQueue

/**
  * Created by chm on 10/25/2016.
  */
class MultiProbing {

  implicit object Ord extends Ordering[Int] {
    def compare(x: Int, y:Int) = y.compare(x)
  }

  val pq = new mutable.PriorityQueue[(Int,Int)]()

  val perturbationV=null
  val sequenceP=null
  val scores=new Array[Double](0)


}
