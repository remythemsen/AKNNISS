import java.io.{FileInputStream, ObjectInputStream}

import scala.collection.mutable

/**
  * Created by chm on 12/16/2016.
  */
object LoadKNN {

  var finalHashMap = new mutable.HashMap[Int, Array[(Int, Float)]]()
  var index = 0

  def concatKNN(): mutable.HashMap[Int, Array[(Int, Float)]]={
    var knnstructureNum = 5
    for (i <- 0 until knnstructureNum) {
    index += 1
    println ("Loading KNN Structure")
    val objReader = new ObjectInputStream (new FileInputStream ("data/knnstructure".concat (index.toString () ) ) )
    //KNNStructure
    val hashMap = objReader.readObject.asInstanceOf[mutable.HashMap[Int, Array[(Int, Float)]]]
    for (j <- hashMap) {
    finalHashMap += j
    }
    objReader.close
    }
    finalHashMap
  }
}