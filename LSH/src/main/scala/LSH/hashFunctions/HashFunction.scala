package LSH.hashFunctions

import utils.tools._
import scala.util.Random

trait HashFunction {
  def apply(v:Array[Float]) : String
}

case class Hyperplane(k:Int, rndf:() => Random, numOfDim:Int) extends HashFunction {
  val rnd = rndf()
  val numberOfDimensions = numOfDim

  val hyperPlanes = for {
    i <- 0 until k
    hp <- List(generateRandomV(numberOfDimensions))
  } yield hp

  def apply(v:Array[Float]) = {
    val res = for {
      hp <- hyperPlanes
      r <- List(hash(v,hp))
    } yield r
    res.mkString
  }

  def hash(v: Array[Float], randomV: Array[Float]): Int = {
    if (Distance.parDotProduct(v, randomV) > 0) 1 else 0
  }

  def generateRandomV(size: Int) : Array[Float] = {
    val set = for {
      i <- 0 until size
      c <- Array[Float]({
        if (rnd.nextBoolean()) -1 else 1
      })
    } yield c

    set.toArray
  }
}
