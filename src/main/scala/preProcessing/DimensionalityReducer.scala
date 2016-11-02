package preProcessing

import breeze.linalg.{DenseMatrix, PCA}
import breeze.numerics.log
import tools.Distance

import scala.math.{abs, pow, sqrt}
import scala.collection.mutable.ArrayBuffer

/**
  * Created by remeeh on 10/2/16.
  */
object DimensionalityReducer{
  // Principal Component Analysis
  def getNewVector(x:Vector[Double],A: DenseMatrix[Double]):Vector[Double] = {
    // n=number of points in the set
    // d=dimensions of vector
    // return the new vector with reduced dimension after  Ax product
    //get Matrix A// NEW A EVERY TIME SHOULD BE THE SAME FOR THE SET ???
    val y=MatrixVectorProduct(x,A)//return Reduced Vector
    y
  }

  def getRandMatrix(n:Int, d:Int): DenseMatrix[Double] ={
    //generate random matrix A
    //m >= (9 * epsilon *logBase2(n))
    val epsilon=1// 0 <= epsilon <= 1
    val base2 = scala.math.log(2) // natural log of 2
    val log2N = scala.math.log(n) / base2
    // m = new reduced dimension
    val m=((9*epsilon*log2N).toInt) + 1

    val A = DenseMatrix.rand(m, d, breeze.stats.distributions.Gaussian(0, 1))
    val M=normalizeMatrix(A)
    M
  }

  def MatrixVectorProduct(x:Vector[Double],A:DenseMatrix[Double]):Vector[Double]={
    //A*xw
    val buffer= new ArrayBuffer[Double]

    for(i<-0 until A.rows){
      val b = new ArrayBuffer[Double]
      for(j<-0 until x.size){
       b+=A(i,j)
      }
       buffer+=Distance.dotProduct(b.toVector,x)
    }
    // return the new vector with reduced dimensions
    buffer.toVector
  }

  def normalizeMatrix(A:DenseMatrix[Double]):DenseMatrix[Double]={
    val buffer= new ArrayBuffer[Double]

    for(i<-0 until A.rows){
      val b = new ArrayBuffer[Double]
      for(j<-0 until A.cols){
        b+=A(i,j)
      }

      val l= sqrt((b).map { case (x) => pow(x, 2) }.sum)

      for(c <-0 until A.cols){
        A(i,c)=b(c)/l
      }

    }
    // return the new vector with reduced dimensions
    A
  }

}
