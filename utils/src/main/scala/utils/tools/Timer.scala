package utils.tools

/**
  * Created by chm on 12/20/2016.
  */
class Timer {

  var start = 0L
  var spent = 0L

  def Timer():Unit = { play() }
  def check(): Double = {
    (System.nanoTime()-start+spent)/1e9
  }
  def pause():Unit = { spent += System.nanoTime()-start }
  def play():Unit = { start = System.nanoTime() }
}

