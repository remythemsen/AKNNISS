import java.io.{FileInputStream, ObjectInputStream}

import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable

/**
  * Created by remeeh on 12/19/16.
  */

class KNNLoadSpec extends FlatSpec with Matchers {
  "KNNStructure" should "have 30 values in each bucket" in {
    val objReader = new ObjectInputStream(new FileInputStream("data/knnstructure"))
    //KNNStructure
    val hashMap = objReader.readObject.asInstanceOf[mutable.HashMap[Int,Array[(Int,Float)]]]
    objReader.close()

    for(v <- hashMap.toArray) {
      v._2.length should be (31)
    }
  }

  "KNNStructure" should "have 11706 entries" in {
    val objReader = new ObjectInputStream(new FileInputStream("data/knnstructure"))
    //KNNStructure
    val hashMap = objReader.readObject.asInstanceOf[mutable.HashMap[Int,Array[(Int,Float)]]]
    objReader.close()

    hashMap.toArray.length should be (11706)
  }
}
