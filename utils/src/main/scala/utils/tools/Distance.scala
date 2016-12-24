package utils.tools

import scala.annotation.tailrec
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

  def parDotProduct(x: Array[Float], y: Array[Float]): Float = {
    val p = 2
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

  def parallelDotProduct(x: Array[Float], y: Array[Float]): Future[Float] = {
    def go(f:Int, t:Int, a: => Array[Float], b: => Array[Float], res:Float) : Future[Float] = {
      // base case
      if(f == t) {
        Future{ a(f) * b(f) }
      } else {
        // recurse
        go(f, t/2, a,b, res )
      }
    }
    // Call first step
    go(0, x.length, x, y, 0.0f)
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
    val res:Float = 1-Distance.parDotProduct(x, y)
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
