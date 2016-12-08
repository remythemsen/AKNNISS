/**
  * Created by Roxana on 12/8/16.
  */

package reducer

import breeze.linalg.DenseMatrix
import scala.collection.mutable.ArrayBuffer
import scala.math.{pow, sqrt}



object Program {

}


  object Preprocess {
    def getNewVector(x:Vector[Double],A: DenseMatrix[Double]):Vector[Double] = {
      val y = MatrixVectorProduct(x,A) //return Reduced Vector
      y
    }

    def getRandMatrix(n:Int, d:Int): DenseMatrix[Double] ={

      val epsilon = 1// 0 <= epsilon <= 1
      val base2 = scala.math.log(2)
      val log2N = scala.math.log(n) / base2
      // m = new reduced dimension
      val m = ((9*epsilon*log2N).toInt) + 38

      val A = DenseMatrix.rand(m, d, breeze.stats.distributions.Gaussian(0, 1))
      val M = normalizeMatrix(A)
      M
    }

    def MatrixVectorProduct(x:Vector[Double],A:DenseMatrix[Double]):Vector[Double]={
      // A * xw
      val buffer = new ArrayBuffer[Double]
      for(i<-0 until A.rows){
        var b = 0.0
        for(j<-0 until x.size){
          b += A(i,j)*x(j)
        }
        buffer += b
      }
      buffer.toVector
    }

    def normalizeMatrix(A:DenseMatrix[Double]):DenseMatrix[Double]={
      val buffer = new ArrayBuffer[Double]

      for(i<-0 until A.rows){
        val b = new ArrayBuffer[Double]
        for(j<-0 until A.cols){
          b += A(i,j)
        }

        val l= sqrt((b).map { case (x) => pow(x, 2) }.sum)
        for(c <-0 until A.cols){
          A(i,c) = b(c)/l
        }
      }

      A
    }

  }

