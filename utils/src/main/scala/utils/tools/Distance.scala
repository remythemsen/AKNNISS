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
    math.sqrt({
      x.map(i => i*i).sum
    })
  }

  def parallelDotProduct(x: Array[Float], y: Array[Float]) : Float = {
    val p = 4
    val futures:ArrayBuffer[Future[Float]] = new ArrayBuffer()
    for(i <- 0 until p) {
      futures += Future {

        var r:Float = 0.0f
        for(j <- i until x.length by p) {
          r += x(j) * y(j)
        }
        r
      }
    }
    // Merge
    val results = Await.result(Future.sequence(futures), 5.seconds)
    results.sum
  }
  def parDotProduct(x: Array[Float], y: Array[Float]): Float = {
    val p = 8
    val futures:ArrayBuffer[Future[Float]] = new ArrayBuffer()
    for(i <- 0 until p) {
      futures += Future {
        var r:Float = 0.0f
        for(j <- i until x.length by p) {
          r += x(j) * y(j)
        }
        r
      }
    }
    // Merge
    val results = Await.result(Future.sequence(futures), 5.seconds)
    results.sum

  }

  def dotProduct(x: Array[Float], y: Array[Float]): Float = {
    (for((a, b) <- x zip y) yield a * b).sum
  }

  def normalize(x:Array[Float]):Array[Float]={
    val m=magnitude(x).toFloat
    x.map (_/m)
  }
}

case object Cosine extends Distance {
  def measure(x:Array[Float], y:Array[Float]) : Float = {
    // We can do the 2-2 because we are on the units sphere
    val res:Float = 1-(Distance.parDotProduct(x, y)).toFloat
    // normalize result:
    res / 2
  }
}

case object Euclidean extends Distance {
  def measure(x:Array[Float], y:Array[Float]) : Float = {
    sqrt((x zip y).map {
      case (a, b) => pow(b - a, 2)
    }.sum).toFloat
  }
}
