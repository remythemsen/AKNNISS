package IO
import scala.io.Source
import scala.collection.mutable.ListBuffer

/**
  * Created by remeeh on 9/26/16.
  */
object Parser {

  def parseInput(fileName:String) = {
    val data = Source.fromFile(fileName).getLines
    convertToTuples(data)
  }

/*  def parseInputUntil(fileName:String, n:Int) = {
    val data = Source.fromFile(fileName).getLines().take(n)
    convertToTuples(data)
  }*/

  // a sample of 666 gives 99% confidence
  // that mean is +/- 5% from population
  def getDistributedSample(sampleSize:Int) = {}


  private def convertToTuples[A](l:Iterator[String]) = {
    var id:String = "";
    var res:ListBuffer[(String, Vector[Double])] = ListBuffer[(String, Vector[Double])]()

    l.zipWithIndex.collect {
      case (e,i) => {
        if (((i+1) % 2) == 1) {
          id = e.toString.substring(49)
        } else {
          // Add new tuple to list
          res += Tuple2(id, e.toString.split(" ").map(x => x.toDouble).toVector)
        }
      }
    }

    // return result
    res.toList
  }
}
