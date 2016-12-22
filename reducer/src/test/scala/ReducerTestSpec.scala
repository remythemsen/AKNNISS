

import java.io.{BufferedWriter, File, FileWriter}

import utils.IO.DisaFileParser
import breeze.linalg.DenseMatrix
import breeze.stats.distributions.Gaussian
import com.sun.deploy.config.Config
import reducer.{Config, DimensionalityReducer}
import utils.tools.{Cosine, Distance}
import utils.IO.ReducedFileParser

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  * Created by chm on 12/12/2016.
  */
object ReducerTestSpec {
  val rnd = new Gaussian(0,1)

  def main(args: Array[String]) = {

    val A = DimensionalityReducer.getRandMatrix(20000000, 4096, rnd);
    val queryParser = new DisaFileParser(new File("C:/Users/chm/Desktop/IdeaProjects/AKNNISS/descriptors-mini.data"))
    val query = queryParser.next
    val query2=queryParser.next
    println("query: " + query._1 +" query2: "+query2._1)

    val vec1=query._2
    val vec2=query2._2
    println("Distance "+ Cosine.measure(vec1,vec2))

    val arr = new Array[Float](256)
    DimensionalityReducer.getNewVector(vec1, A, arr)
    val arr2=new Array[Float](256)
    DimensionalityReducer.getNewVector(vec2,A,arr2)
    println(arr.size+" "+arr2.size)
    println("Distance2 "+ Cosine.measure(arr,arr2) )
    var x= new ArrayBuffer[Int](10)


  }

}
