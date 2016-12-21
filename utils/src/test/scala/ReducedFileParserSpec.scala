import org.scalatest.{FlatSpec, Matchers}
import java.io.File
import utils.IO.ReducedFileParser
import scala.collection.mutable.ArrayBuffer

/**
  * Created by remeeh on 12/21/16.
  */
class ReducedFileParserSpec extends FlatSpec with Matchers {


  "Vectors" should "have 256 d" in {
    val vectors = new ReducedFileParser(new File("data/dimensionalTest"))
    //check levtod dimension
    var count=0
    var ids:ArrayBuffer[Int] = new ArrayBuffer[Int]
    while(vectors.hasNext){
    //vectors.next._2.length should be (256)
    var v = vectors.next
      if(v._2.length < 256) {
        ids += v._1
        count+=1
      }
    }
    for(i <- ids) {
      println(i)
    }
    println(count)
    count should be (0)
  }
}
