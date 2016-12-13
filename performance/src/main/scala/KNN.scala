import java.io.File

import utils.IO.ReducedFileParser
import utils.tools.Distance

import scala.collection.mutable
import scala.math.Ordering

class KNN {
  // TODO: pass number of probes for each CP
  var maxHeap = new mutable.PriorityQueue[(Int,Float)]()(Ord)

  implicit object Ord extends Ordering[(Int,Float)] {
    def compare(x:(Int,Float), y:(Int,Float)) = x._2.compare(y._2)
  }

  def findKNearest(q:(Int,Array[Float]), k:Int,fileName:String, distance:Distance) = {

    val data = new ReducedFileParser(new File(fileName))

    // Take out first k, return candidate-set
    var i = 0
    while(data.hasNext){

      while(i < 30){
        var tmpTuple = data.next
        maxHeap.enqueue((tmpTuple._1,distance.measure(q._2,tmpTuple._2)))
        i+=1
      }

      var tuple = data.next

      if(distance.measure(q._2,tuple._2) < maxHeap.head._2){
        maxHeap.dequeue()
        maxHeap.enqueue((tuple._1,distance.measure(q._2,tuple._2)))
      }
    }
    maxHeap.toArray
  }
}
