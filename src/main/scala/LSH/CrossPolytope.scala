package LSH

import com.lowagie.text.pdf.parser.Matrix

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/**
  * Created by remeeh on 9/26/16.
  */
class CrossPolytope extends LSH {
  def isSparse(x: Vector[Double]): Boolean = {
    // TODO: return true if sparse, otherwise false
    false
  }

  // TODO: implement Matrix object
  def generateRandomSparseMatrixS(oldD: Int, newD: Int, seed: Long): Array[Array[Double]] = {
    // S - random sparse d x d’ matrix, whose columns have one non-zero, ±1 entry sampled uniformly)
    // TODO: how to generate S?
    val matrixS = Array.ofDim[Double](oldD, newD)
    matrixS
  }

  def generateRandomDiagonalMatrixD(size: Int, seed: Long): Array[Array[Double]] = {
    // D - random diagonal {±1} matrix (used for “flipping signs”)
    // TODO: how to generate D?
    val matrixD = Array.ofDim[Double](size, size)
    matrixD
  }

  def generateHadamardMatrixH(size: Int, seed: Long): Array[Array[Double]]  = {
    // H - Hadamard matrix
    // TODO: how to generate H?
    val matrixH = Array.ofDim[Double](size, size)
    return matrixH
  }

  def featureHashing(x: Vector): Vector  = {
    // for sparse vector x, return x'
    // apply a linear map x ⟶ Sx
    val newX: Vector = null // declaration
    // d' is 1024 for now; change later
    val S = generateRandomSparseMatrixS(x.size, 1024, System.currentTimeMillis())
    // x’ = Sx
    // TODO: create a matrix multiplication method
    newX
}


  // compute pseudorandom rotation: Fast Hadamard Transform
  def computeHash(x: Vector): Vector = {
    val D1 = generateRandomDiagonalMatrixD(x.size, System.currentTimeMillis())
    val D2 = generateRandomDiagonalMatrixD(x.size, System.currentTimeMillis())
    val D3 = generateRandomDiagonalMatrixD(x.size, System.currentTimeMillis())

    val hashVal: Vector = null // declaration
    // y = HD1HD2HD3x // matrix multiplication
    // TODO: how to return closest point (basis vector) to y?
    //  return closest point {±ei} (corner of polytope) to y
    hashVal
  }

  // Hashing:
  // preprocessing
  //if(isSparse(x))
  // perform feature hashing
    //x’ = featureHashing(x)
  //computeHash(x’)

  def hash(x: Vector[Double]): Int = {
    0
  }

  // TODO: store hash of x’ in hashtable
  
  override def build(data: Stream[(String, Vector[Double])], k: Int, l: Int): LSHStructure = ???
}
