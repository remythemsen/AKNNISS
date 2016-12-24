import java.io.File

import utils.tools._
import org.scalatest.{FlatSpec, Matchers}
import utils.IO.ReducedFileParser

import scala.io.Source

/**
  * Created by remeeh on 12/24/16.
  */
class ReduceSpeedSpec extends FlatSpec with Matchers {
  val vlength = new ReducedFileParser(new File("data/descriptors-decaf-10k.data")).size
  val lines:ReducedFileParser = new ReducedFileParser(new File("data/descriptors-decaf-10k.data"))
  val varray = new Array[Array[Float]](vlength)
  val timer = new Timer
  for(x <- varray.indices) {
    varray(x) = lines.next._2
  }
  val n = 5
  println("warmup")
  for(i <- 0 until n) {
    println(i)
    for(x <- varray.indices) {
      Distance.parDotProduct(varray(x), varray(x))
    }
  }

  var avg0 = 0.0
  var avg = 0.0
  var avg2 = 0.0
  println("Starting test 0")
  for(i <- 0 until n) {
    println(i)
    for(x <- varray.indices) {
      timer.play()
      Distance.dotProduct(varray(x), varray(x))
      avg0+=timer.check
    }
  }
  println("Starting test 1")
  for(i <- 0 until n) {
    println(i)
    for(x <- varray.indices) {
      timer.play()
      Distance.parDotProduct(varray(x), varray(x))
      avg+=timer.check
    }
  }
  println("Starting test 2")
  for(i <- 0 until n) {
    println(i)
    for(x <- varray.indices) {
      timer.play()
      Distance.parallelDotProduct(varray(x), varray(x))
      avg2+=timer.check
    }
  }
  println(avg0/n*vlength)
  println(avg/n*vlength)
  println(avg2/n*vlength)

}
