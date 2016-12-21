import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}

import scala.collection.mutable

object Program extends App {
  println("Hello world")
  val knns = loadKNNStructure
  println("Structure loaded")


  // making a new structure
  val rknns = new mutable.HashMap[Int, Array[(Int, Float)]]()

  for(k <- knns.toArray) {
    rknns += (k._1 -> k._2.slice(20,30).sortBy(x => x._2))
  }

  println("Saving structure to disk...")
  val oos = new ObjectOutputStream(new FileOutputStream("data/knnstructure_"+rknns.size))
  oos.writeObject(rknns)
  oos.close
  println("structure was saved..")

  println("Done")

  def loadKNNStructure = {
    println("Loading KNN Structure")
    val objReader = new ObjectInputStream(new FileInputStream("data/knnstructure"))
    //KNNStructure
    val hashMap = objReader.readObject.asInstanceOf[mutable.HashMap[Int,Array[(Int,Float)]]]
    objReader.close
    hashMap
  }
}
