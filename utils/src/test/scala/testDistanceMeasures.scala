import java.io.File

import utils.IO.ReducedFileParser
import utils.tools.{Cosine, Distance, Euclidean, Timer}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by chm on 12/23/2016.
  */
object testDistanceMeasures {

  def main(args: Array[String]): Unit = {

    var tb=new ArrayBuffer[Double]()
    val queries = new ReducedFileParser(new File("data/queries-5111.data"))
    val dataset=new ReducedFileParser(new File("data/descriptors-decaf-100k.data"))

    val warmUpqueries = new ReducedFileParser(new File("data/queries.data"))
    val warmUpData=new ReducedFileParser(new File("data/queries-sample-fixed.data"))

    while(warmUpqueries.hasNext){
      val query=warmUpqueries.next._2
      while(warmUpData.hasNext){
        val data=warmUpData.next._2
        val dist=Cosine.measure(query,data)
      }
    }

    val time=new Timer()
    time.check()
    while(queries.hasNext){
      val query=queries.next._2
      while(dataset.hasNext){
        val data=dataset.next._2
        time.play()
        val dist=Cosine.measure(query,data)
        tb += time.check()
      }
    }

    println(tb.sum/tb.size)
  }
}
