import scala.io.Source

/**
  * Created by remeeh on 12/19/16.
  */
class probeConfigParser(probeconfig:String) {
  val file:Iterator[String] = Source.fromFile(probeconfig).getLines

  def hasNext:Boolean = file.hasNext
  def next:ProbeConfig = {
    val config = file.next.toString.split(" ")
    ProbeConfig(
      config(0).toInt, // N
      config(1).toInt, // m
      config(2), // hashFunction
      config(3).toInt, //d
      config(4), //datafile
      config(5), //warmupfile
      config(6), //probing scheme
      config(7).toInt //num of probes
    )
  }
}
