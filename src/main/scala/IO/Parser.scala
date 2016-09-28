package IO
import scala.io.Source
import scala.collection.mutable.ListBuffer

/**
  * Created by remeeh on 9/26/16.
  */
object Parser {

  def parseInput(fileName:String) = {
    val data = Source.fromFile(fileName).getLines.toList
    convertToTuples(data)
  }

  def parseInputUntil(fileName:String, n:Int) = {
    val data = Source.fromFile(fileName).getLines().take(n).toList
    convertToTuples(data)
  }

  // a sample of 666 gives 99% confidence
  // that mean is +/- 5% from population
  def getDistributedSample(sampleSize:Int) = {}


  private def convertToTuples[A](l:List[A]) = {
    var id:String = "";
    var res:ListBuffer[(String, List[Double])] = ListBuffer[(String, List[Double])]()

    l.zipWithIndex.collect {
      case (e,i) => {
        if (((i+1) % 2) == 1) {
          id = e.toString.substring(49)
        } else {
          // Add new tuple to list
          res += Tuple2(id, e.toString.split(" ").map(x => x.toDouble).toList)
        }
      }
    }

    // return result
    res.toList
  }
}
