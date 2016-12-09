/**
  * Created by Roxana on 12/8/16.
  */

package reducer

import java.io._
import java.util.concurrent.ArrayBlockingQueue

import IO.DisaFileParser
import breeze.linalg.DenseMatrix
import utils.tools.Distance

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.io.Source
import scala.math.{pow, sqrt}
import scala.concurrent.ExecutionContext.Implicits.global

case class Config(data:File, outDir:String)

object Program extends App {

  val config = new Config(new File("reducer/data/descriptors-decaf-random-sample.data"), "./tablehandler/data")
  val randomMatrix:DenseMatrix[Float]= DimensionalityReducer.getRandMatrix(20000000,4096);

  var n = 0
  val p = 4 // Number of threads
  val loadedTuples = new ArrayBlockingQueue[(Int, Array[Float])](5000)
  val preProcessedTuples = new ArrayBlockingQueue[(Int, Array[Float])](50)

  val input = new DisaFileParser(config.data)
  n = input.size
  var progress = 0

  Future {
    while(input.hasNext) {
      loadedTuples.put(input.next)
    }
  }

  for(i <- 0 until p) {
    Future {
      while(true) {
        var tuple = loadedTuples.take()
        preProcessedTuples.put(tuple._1, DimensionalityReducer.getNewVector(tuple._2, randomMatrix))
      }
    }
  }

  val dir:String = config.outDir.concat("/")
    // constructing filename
    .concat(config.data.getName.substring(0,config.data.getName.length-5))
    .concat("-reduced.data")

  val output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dir.toString)))

  var j = 0.0

  Future {
    while(true) {

      var t = preProcessedTuples.take()
      val sb = new StringBuilder
      sb.append(t._1)
      sb.append(" ")
      for(component <- t._2) {
        sb.append(component+" ")
      }
      sb.append("\n")

      // Write resulting set
      output.write(sb.toString())

      j+=1.0
      progress += 1
      if(j % 100 == 0) {
        println(((j / n) * 100).toInt.toString +"%")
      }
    }
  }.onFailure {
    case t => println("An error has occured: " + t.getMessage)
  }

  while(progress < n) {
    Thread.sleep(200)
  }
  println("Finished with "+n+" tuples")

}

object DimensionalityReducer{

  def getNewVector(x:Array[Float],A: DenseMatrix[Float]):Array[Float] = {
    val y=MatrixVectorProduct(x,A)//return Reduced Vector
    y
  }

  def getRandMatrix(n:Int, d:Int): DenseMatrix[Float] ={

    val epsilon=1// 0 <= epsilon <= 1
    val base2 = scala.math.log(2)
    val log2N = scala.math.log(n) / base2
    // m = new reduced dimension
    val m=((9*epsilon*log2N).toInt) + 2

    val A = DenseMatrix.rand(m, d, breeze.stats.distributions.Gaussian(0, 1))
    val M=normalizeMatrix(A)
    M
  }

  def MatrixVectorProduct(x:Array[Float],A:DenseMatrix[Float]):Array[Float]={
    //A*xw
    val reduced:Array[Float] = new Array(A.rows)
    // Reusable array
    val b:Array[Float] = new Array(x.length)

    for(i<-0 until A.rows){
      for(j<-x.indices){
        b(j) = A(i,j) // filling b with new values
      }
      reduced(i) = Distance.parDotProduct(b,x)
    }
    reduced
  }

  def normalizeMatrix(A:DenseMatrix[Double]):DenseMatrix[Float]={
    val buffer= new ArrayBuffer[Float]

    val B:DenseMatrix[Float] = DenseMatrix.zeros(A.rows, A.cols)

    for(i<-0 until A.rows){
      val b = new ArrayBuffer[Double]
      for(j<-0 until A.cols){
        b+=A(i,j) // Note this conversion
      }
      val l= sqrt(b.map { case (x) => pow(x, 2) }.sum)
      for(c <-0 until A.cols){
        B(i,c) = (b(c)/l).toFloat
      }
    }
    B
  }
}

