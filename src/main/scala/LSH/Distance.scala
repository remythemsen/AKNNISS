package LSH

import math.{pow, sqrt, abs}

/**
  * Created by remeeh on 10/10/16.
  * Measures developed by chris matrakou
  */

trait Distance {
  def measure(x:Vector[Double], y:Vector[Double]) : Double

  def magnitude(x: Vector[Double]): Double = {
    sqrt((x).map { case (x) => pow(x, 2) }.sum)
  }

  def normalize(x:Vector[Double]):Vector[Double]={
    val m=magnitude(x);
    (x).map { case (x) =>(x/m) }
  }

}

object Distance {
  def dotProduct(x: Vector[Double], y: Vector[Double]): Double = {
    (x zip y).map { case (x, y) => (y * x) }.sum
  }
}

case object Cosine extends Distance {
  def measure(x:Vector[Double], y:Vector[Double]) : Double = {
    1-((Distance.dotProduct(x, y)) / (magnitude(x) * magnitude(y)))
  }
}

case object Euclidean extends Distance {
  def measure(x:Vector[Double], y:Vector[Double]) : Double = {
    sqrt((x zip y).map { case (x, y) => pow(y - x, 2) }.sum)
  }
}

case object Manhattan extends Distance {
  def measure(x:Vector[Double], y:Vector[Double]) : Double = {
    (x zip y).map { case (x, y) => abs(y - x) }.sum
  }
}

case object LInfinityNorm extends Distance {
  def measure(x:Vector[Double], y:Vector[Double]) : Double = {
    (x zip y).map { case (x, y) => abs(y - x) }.max
  }
}

case object Hamming extends Distance {
  def measure(x:Vector[Double], y:Vector[Double]) : Double = {
    (x zip y).count { case (x, y) => (y != x) }
  }
}
