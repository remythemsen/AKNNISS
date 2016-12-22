package LSH.multiprobing

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.math._

class MultiProbingCrossPolytope(listRotations:Array[Array[Float]], ArraymaxIndices:Array[Int], T:Int){

  var pq = new mutable.PriorityQueue[(IndexedSeq[Int],Float)]()(Ord)

  // M = # of rotations (functions)
  val M = listRotations.size

  // d = dimensionality of vectors
  val d = listRotations(0).size
  implicit object Ord extends Ordering[(IndexedSeq[Int],Float)] {
    def compare(x:(IndexedSeq[Int],Float), y:(IndexedSeq[Int],Float)) = y._2.compare(x._2)
  }

  // pairsLists = lists of pairs of distances and indices to the max value, for each CP
  var pairsLists = new Array[Array[(Float,Int)]](M)

  // create lists of pairs
  for(i<-0 until listRotations.size){
    val y = listRotations(i)
    val maxVal = y(ArraymaxIndices(i))
    var pairs:Array[(Float,Int)] = new Array(2*d)
    for(j<-0 until d) {
      pairs(j) = (math.abs(maxVal - y(j)), j)
      pairs(j +y.size ) = (math.abs(maxVal + y(j)), j)
    }

    // sort pairs by distance
    scala.util.Sorting.quickSort(pairs)

    // add pairs to pairList for corresponding cp vector
    pairsLists(i) = pairs
  }


  def generateProbes(): ArrayBuffer[Array[Int]]={

      // pertSetList = list of perturbation sets
      val pertSetList = generateSets(T)

      // listBuckets = list of probing buckets
      val listBuckets = generateProbingBuckets(pertSetList)
    listBuckets
  }

  val perturbationV = null
  val sequenceP = null
  val scores = new Array[Double](0)

  def score(a: IndexedSeq[Int]): Float = {
    // returns the score of a perturbation set
    var score=0.0f

    for(i<-0 until a.size) {
      val curList = pairsLists(i)
      val pair = curList(a(i))
      val dist = pair._1
      score += dist*dist
    }
    score
  }

  def shift(A: IndexedSeq[Int]): IndexedSeq[Int] = {
    // replaces last element of A by 1 + the element's value
    val index = A.size-1
    val newVal = A(index)+1
    val B = A.updated(index, newVal)
    B
  }

  def expand(A: IndexedSeq[Int]): IndexedSeq[Int] = {
    // adds the last element + 1 to the set
    val B = A:+0
    B
  }

  def generateSets(Tsize:Int):Array[IndexedSeq[Int]]={
    val setsList = new Array[IndexedSeq[Int]](Tsize)

    // initialize the heap with the element 0, with the score of 0
    pq.enqueue((IndexedSeq(0),0.0f))

    // i counts the number of sets that are added to the setsList
    var i = 0
    var done = false

    do{
      // extract the element of minimum score from the heap
      val ps = pq.dequeue()._1

      // when perturbation set "complete", add to the setsList
      if(ps.size == M){
        setsList(i) = ps

        // add the shifted perturbation set to the heap
        val shiftedPs = shift(ps)
        val scoreShiftedPs = score(shiftedPs)
        pq.enqueue((shiftedPs, scoreShiftedPs))

        if(i < Tsize - 1){
          i += 1
        } else {
          done = true
        }
      } else {
        // add the shifted perturbation set to the heap
        val shiftedPs = shift(ps)
        val scoreShiftedPs = score(shiftedPs)
        pq.enqueue((shiftedPs, scoreShiftedPs))

        // add the expanded perturbation set to the heap
        val expandedPs = expand(ps)
        val scoreExpandedPs = score(expandedPs)
        pq.enqueue((expandedPs, scoreExpandedPs))
      }
    } while( !done )

    setsList
  }

  def generateProbingBuckets(setsList:Array[IndexedSeq[Int]]): ArrayBuffer[Array[Int]]={
    val T = setsList.size
    val listOfProbingBuckets = new ArrayBuffer[Array[Int]](T)
      for(i<-0 until T){
        val probingBucket = new Array[Int](M)
        for(j<-0 until M){
          // get the right CP
          val listOfPairs = pairsLists(j)

          // retrieve the corresponding pair
          val pair = listOfPairs(setsList(i)(j))

          // get the index of the next closest point to the query
          val indexInVector = pair._2

          // compute the hash values of the probing bucket
          if(listRotations(j)(indexInVector) < 0){
            probingBucket(j) = 2 * indexInVector - 2
          } else {
            probingBucket(j) = 2 * indexInVector - 1
          }
      }
      listOfProbingBuckets += probingBucket
    }
    listOfProbingBuckets
  }
}
