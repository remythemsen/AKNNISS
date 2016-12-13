import scala.collection.mutable
import scala.math.Ordering

/**
  * Created by chm on 12/13/2016.
  */
object KNNTest {
  var maxHeap = new mutable.PriorityQueue[(Int,Float)]()(Ord)

  implicit object Ord extends Ordering[(Int,Float)] {
    def compare(x:(Int,Float), y:(Int,Float)) = x._2.compare(y._2)
  }
  def main(args: Array[String]) = {

      maxHeap.enqueue((1,1.2f))
      maxHeap.enqueue((2,6.1f))
      maxHeap.dequeue()
      maxHeap.enqueue((3,2.3f))
      maxHeap.enqueue((4,8.4f))
      maxHeap.enqueue((5,1.1f))
    println(4>maxHeap.head._2)

    while(maxHeap.head!=null){
      println(maxHeap.dequeue())
    }
  }

}
