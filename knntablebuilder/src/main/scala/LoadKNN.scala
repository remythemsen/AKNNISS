import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}

import scala.collection.mutable

object LoadKNN {

  var finalHashMap = new mutable.HashMap[Int, Array[(Int, Float)]]()
  var index = 0

  def concatKNN(): Unit={
    var knnstructureNum = 5
    for (i <- 0 until knnstructureNum) {
      index += 1
      println ("Loading KNN Structure")
      val objReader = new ObjectInputStream (new FileInputStream ("data/knnstructure-".concat (index.toString () ) ) )

      //KNNStructure
      val hashMap = objReader.readObject.asInstanceOf[mutable.HashMap[Int, Array[(Int, Float)]]]
      for (j <- hashMap) {
        finalHashMap += j
      }
      objReader.close
      }

      val oos = new ObjectOutputStream(new FileOutputStream("data/knnstructure"))
      oos.writeObject(finalHashMap)
      oos.close
      println("Merged KNN structure was saved.")
  }


}