import java.io._

import utils.IO.ReducedFileParser

import scala.collection.mutable

object Program extends App {
  println("Hello world")
  val knns = loadKNNStructure
  println("Structure loaded")

  val parser = new ReducedFileParser(new File("data/queries-1086-norm.data"))

  // making a new structure
  val rknns = new mutable.HashMap[Int, Array[(Int, Float)]]()

  while(parser.hasNext) {
    val q = parser.next
    if(!knns.contains(q._1)) println(q._1)
  }

/*  println("Saving structure to disk...")
  val oos = new ObjectOutputStream(new FileOutputStream("data/1008934_4993_Cosine.knnstructure"))
  oos.writeObject(rknns)
  oos.close
  println("structure was saved..")*/

  println("Done")

  def loadKNNStructure = {
    println("Loading KNN Structure")
    val objReader = new ObjectInputStream(new FileInputStream("data/4372232_1086_Cosine.knnstructure"))
    //KNNStructure
    val hashMap = objReader.readObject.asInstanceOf[mutable.HashMap[Int,Array[(Int,Float)]]]
    objReader.close
    hashMap
  }
}
