import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object QuickSelect {
  def quickSelect[A <% Ordered[A]](seq: Seq[A], n: Int, rand: Random = new Random): A = {
    val pivot = rand.nextInt(seq.length);
    val (left, right) = seq.partition(_ < seq(pivot))
    if (left.length == n) {
      seq(pivot)
    } else if (left.length < n) {
      quickSelect(right, n - left.length, rand)
    } else {
      quickSelect(left, n, rand)
    }
  }

  
}

