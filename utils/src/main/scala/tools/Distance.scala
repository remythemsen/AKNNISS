package utils.tools

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.math.{abs, pow, sqrt}

trait Distance {
  def measure(x:Array[Float], y:Array[Float]) : Float
}

object Distance {
  def magnitude(x: Array[Float]): Double = {
    math.sqrt(x map(i => i*i) sum)
  }
  def parallelDotProduct(x: Array[Float], y: Array[Float]) : Float = {
    val p = 4
    val futs:ArrayBuffer[Future[Float]] = new ArrayBuffer()
    for(i <- 0 until p) {
      futs += Future {

        var r:Float = 0.0f
        for(j <- i until x.length by p) {
          r += x(j) * y(j)
        }
        r
      }
    }
    // Merge
    val results = Await.result(Future.sequence(futs), 5 second)
    results.sum
  }
  def parDotProduct(x: Array[Float], y: Array[Float]): Float = {
    val p = 8
    val futs:ArrayBuffer[Future[Float]] = new ArrayBuffer()
    for(i <- 0 until p) {
      futs += Future {
        var r:Float = 0.0f
        for(j <- i until x.length by p) {
          r += x(j) * y(j)
        }
        r
      }
    }
    // Merge
    val results = Await.result(Future.sequence(futs), 5 second)
    results.sum

  }
  def dotProduct(x: Array[Float], y: Array[Float]): Float = {
    (for((a, b) <- x zip y) yield a * b) sum
  }
  def normalize(x:Array[Float]):Array[Float]={
    val m=magnitude(x)
    (x).map { case (x) =>(x/m).toFloat}
  }
}

case object Cosine extends Distance {
  def measure(x:Array[Float], y:Array[Float]) : Float = {
    // We can do the 2-2 because we are on the units sphere
    val res:Float = sqrt(2-(2*(Distance.parDotProduct(x, y)/(Distance.magnitude(x) * Distance.magnitude(y))))).toFloat
    // normalize result:
    res / 2
  }
}

case object Euclidean extends Distance {
  def measure(x:Array[Float], y:Array[Float]) : Float = {
    sqrt((x zip y).map { case (x, y) => pow(y - x, 2) }.sum).toFloat
  }
}

case object Manhattan extends Distance {
  def measure(x:Array[Float], y:Array[Float]) : Float = {
    (x zip y).map { case (x, y) => abs(y - x) }.sum
  }

}

case object LInfinityNorm extends Distance {
  def measure(x:Array[Float], y:Array[Float]) : Float = {
    (x zip y).map { case (x, y) => abs(y - x) }.max
  }
}

case object Hamming  {
  def measure(x:Array[Int], y:Array[Int]) : Int = {
    (x zip y).count { case (x, y) => (!y.equals(x)) }
  }
}

