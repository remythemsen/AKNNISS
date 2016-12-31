import scala.collection.mutable
import scala.math.Ordering

/**
  * Created by christo on 26-Dec-16.
  */
object KNNtest {

  var maxHeap = new mutable.PriorityQueue[(Int,Float)]()(Ord)

  implicit object Ord extends Ordering[(Int,Float)] {
    def compare(x:(Int,Float), y:(Int,Float)) = x._2.compare(y._2)
  }

  def main(args: Array[String]): Unit = {
    maxHeap.enqueue((14,1.3f))
    maxHeap.enqueue((5,2.3f))
    maxHeap.enqueue((2,4.3f))
    maxHeap.enqueue((3,6.3f))

    println(maxHeap.dequeue())
  }
}
