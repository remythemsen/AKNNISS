import java.io.{FileInputStream, ObjectInputStream}

import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable
import java.io.File

import utils.IO.ReducedFileParser

/**
  * Created by remeeh on 12/21/16.
  */
class ReducedFileParserSpec extends FlatSpec with Matchers {


  "Vectors" should "have 256 d" in {
    val vectors = new ReducedFileParser(new File("data/descriptors-decaf-1m.data"))
    //check levtod dimension
  var count=0
    while(vectors.hasNext){
    //vectors.next._2.length should be (256)
      if(vectors.next._2.length<256){
        count+=1
      }
    }
    println(count)
    count should be (0)
  }
}
