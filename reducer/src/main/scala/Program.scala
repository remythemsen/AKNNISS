/**
  * Created by Roxana on 12/8/16.
  */

package reducer

import java.io._

import breeze.linalg.DenseMatrix
import utils.tools.Distance

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.math.{pow, sqrt}

case class Config(data:File, outDir:String)

object Program extends App {

  val config = new Config(new File("reducer/data/descriptors-decaf-random-sample.data"), "./tablehandler/data")

  val A:DenseMatrix[Float]= DimensionalityReducer.getRandMatrix(20000000,4096);

  // Reducing and saving
  val input = new BufferedInputStream( new FileInputStream( config.data ) )

  // Save LSHStructure to file.
  val dir:String = config.outDir.concat("/")
    // constructing filename
    .concat(config.data.getName.substring(0,config.data.getName.length-5))
    .concat("-reduced.data")

  val output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dir.toString)))
  val input2 = Source.fromFile(config.data.getAbsoluteFile).getLines()

  var j = 0.0

  val size = Source.fromFile(config.data.getAbsolutePath).getLines().length
  println(size)

  // IDEA: Can we have one thread reading, and one writing ?

  var sb = new StringBuilder
  while(input2.hasNext) {
    val l = input2.next
    if(l.charAt(0).equals('#')) {
      // Then get the ID
      sb.append(l.substring(49)+" ")
    } else {
      // Get the vector
      val v = DimensionalityReducer.getNewVector(l.split(" ").map(x => x.toFloat), A)
      for(i <- v) {
        sb.append(i + " ")
      }
      sb.append("\n")

      // Write resulting set
      output.write(sb.toString())

      j+=1.0
      if(j % 100 == 0) {
        println(((j / size) * 100).toInt.toString +"%")
      }
      sb = new StringBuilder
    }
  }
  println("Finished with "+size+" tuples")}

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

