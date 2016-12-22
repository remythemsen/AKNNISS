import java.io.File

import scala.io.Source

/**
  * Created by chm on 12/20/2016.
  */
class Timer {

  var start = 0L
  var spent = 0L

  def Timer() { play() }
  def check(): Double = {
    (System.nanoTime()-start+spent)/1e9
  }
  def pause() { spent += System.nanoTime()-start }
  def play() { start = System.nanoTime() }
}

object  testTime{
  def main(args: Array[String]): Unit = {
    val t = new Timer
    println(t.check())

    println((1.23231132 * 10000).round / 10000.toFloat)
//    println(1.3/2)
//    println(Source.fromFile(new File("data/speedconfig.txt")).getLines().size)

  }
}