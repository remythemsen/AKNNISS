package utils.tools
import scala.util.Random

object QuickSelect {
  def quickSelect[A <% Ordered[A]](seq: Seq[A], n: Int, rand: Random = new Random): A = {
    val pivot = rand.nextInt(seq.size)
    val (left, right) = seq.partition(_ < seq(pivot))
    if (left.size == n) {
      seq(pivot)
    } else if (left.size < n) {
      quickSelect(right, n - left.size, rand)
    } else {
      quickSelect(left, n, rand)
    }
  }

}

