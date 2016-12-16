package LSH.hashFunctions

import utils.tools._

import scala.util.Random

trait HashFunction {
  def apply(v: Array[Float]): Array[Int]
}

case class Hyperplane(k: Int, rndf:() => Random, numOfDim: Int) extends HashFunction {
  val rnd = rndf()
  val numberOfDimensions = numOfDim

  val hyperPlanes = for {
    i <- (0 until k).toArray
    hp <- Array(generateRandomV(numberOfDimensions))
  } yield hp

  // TODO dont convert to array
  def apply(v: Array[Float]):Array[Int] = {
    val res = for {
      hp <- hyperPlanes
      r <- Array(hash(v, hp))
    } yield r
    res
  }

  def hash(v: Array[Float], randomV: Array[Float]): Int = {
    if (Distance.parDotProduct(v, randomV) > 0) 1 else 0
  }

  def generateRandomV(size: Int) : Array[Float] = {
    val set = for {
      i <- (0 until size).toArray
      c <- Array[Float]({
        if (rnd.nextBoolean()) -1 else 1
      })
    } yield c

    set
  }
}

case class CrossPolytope(k: Int, rndf:() => Random, numOfDim: Int) extends HashFunction {
  val rnd = rndf()
  val numberOfDimensions = numOfDim

  val ds = k * 3
  var rotations = new Array[Array[Float]](k)
  var arrayOfMaxIndices = new Array[Int](k)

  val diagonals = new Array[Array[Float]](ds)
  for(i <- 0 until ds){
    diagonals(i) = generateRDV(numberOfDimensions, rnd.nextLong())
  }


  def generateRDV(size: Int, seed: Long): Array[Float] = {
    // D - random diagonal matrix of {±1} (used for “flipping signs”)
    val rnd = new Random(seed)
    val diagonalMatrix = new Array[Float](size)
    for(i<-0 until size){
      if (rnd.nextBoolean()) diagonalMatrix(i) = -1
      else diagonalMatrix(i) = 1
    }

    diagonalMatrix
  }

  def VectorMultiplication(A: Array[Float], x: Array[Float]): Array[Float] = {
    val b = new Array[Float](numberOfDimensions)
    for(i <- 0 until numberOfDimensions){
      b(i) = (A(i) * x(i))
    }
    b
  }

  def hadamardTransformation(a: Array[Float], low: Int, high: Int, y: Array[Float]): Array[Float]={
    if(high - low > 0) {
      var middle = (low + high) / 2
      var c = 1
      for(i <- low until middle + 1){
        y(i) = a(i) + a(middle + c)
        c += 1
      }

      var m = 0
      for(j <- middle + 1 until high + 1){
        y(j) = -a(j) + a(low + m)
        m += 1
      }
      var b = new Array[Float](a.size)
      for(i <- 0 until a.size){
        b(i) = y(i)
      }

      // recursively call the Hadamard transformation method on the 2 halves
      hadamardTransformation(b, low, middle, y)
      hadamardTransformation(b, middle + 1, high, y)
    }
    y
  }


  // compute pseudo-random rotation: Fast Hadamard Transform
  def generateHashcode(x: Array[Float]): Array[Int] = {

    if(x.size != numberOfDimensions) {
      Array(0,0,0)
    }
    else {

      var b = new Array[Float](numberOfDimensions)
      val H = hadamardTransformation(x, 0, numberOfDimensions-1, b)
      val hashcode = new Array[Int](k)
      var index = 0

      // TODO: fix bug that generates vectors of dimensions other than 256


      for (i <- 0 until k) {
        val y = pseudoRandomRotation(H, x, index)
        // Rotations live on global var for multiprobing access
        rotations(i) = y

        var max = 0.0
        var indexOfMax = 0
        for (i <- 0 until numberOfDimensions) {
          if (Math.abs(y(i)) > max) {
            max = y(i)
            indexOfMax = i
          }
        }

        // generate hashing value for each rotation
        if (max > 0) hashcode(i) = 2 * indexOfMax - 1
        else hashcode(i) = 2 * indexOfMax - 2

        arrayOfMaxIndices(i) = indexOfMax

        index += 3
      }

    hashcode
    }
  }

  // Rotation
  def pseudoRandomRotation(H: Array[Float], x: Array[Float], i: Int): Array[Float] ={
    VectorMultiplication(H,
      VectorMultiplication(diagonals(i),
        VectorMultiplication(H,
          VectorMultiplication(diagonals(i + 1),
            VectorMultiplication(H,
              VectorMultiplication(diagonals(i + 2), x))))))
  }

  def apply(x: Array[Float]): Array[Int] = {
    generateHashcode(x)
  }


}