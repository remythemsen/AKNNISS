package tools

import scala.math.{abs, pow, sqrt}

/**
  * Created by remeeh on 10/10/16.
  * Measures developed by chris matrakou
  */

trait Distance {
  def measure(x:Vector[Float], y:Vector[Float]) : Double



}

object Distance {
  def magnitude(x: Vector[Float]): Double = {
    sqrt((x).map { case (x) => pow(x, 2) }.sum)
  }
  def dotProduct(x: Vector[Float], y: Vector[Float]): Double = {
    (x zip y).map { case (x, y) => (y * x) }.sum
  }
  def normalize(x:Vector[Float]):Vector[Float]={
    val m=magnitude(x)
    (x).map { case (x) =>(x/m).toFloat }
  }
}

case object Cosine extends Distance {
  def measure(x:Vector[Float], y:Vector[Float]) : Double = {
    1-((Distance.dotProduct(x, y)) / (Distance.magnitude(x) * Distance.magnitude(y)))
  }
}

case object Euclidean extends Distance {
  def measure(x:Vector[Float], y:Vector[Float]) : Double = {
    sqrt((x zip y).map { case (x, y) => pow(y - x, 2) }.sum)
  }
}

case object Manhattan extends Distance {
  def measure(x:Vector[Float], y:Vector[Float]) : Double = {
    (x zip y).map { case (x, y) => abs(y - x) }.sum
  }

}

case object LInfinityNorm extends Distance {
  def measure(x:Vector[Float], y:Vector[Float]) : Double = {
    (x zip y).map { case (x, y) => abs(y - x) }.max
  }
}

case object Hamming extends Distance {
  def measure(x:Vector[Float], y:Vector[Float]) : Double = {
    (x zip y).count { case (x, y) => (y != x) }
  }
}
