package utils.IO

import java.io._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Created by remeeh on 12/24/16.
  */
object HTMLGenerator extends App {
  val k = 10
  val qs = 10
  val globalSb = new StringBuilder

  println("Starting html printer")
  val cosMap = loadKNNStructure("data/100784_10_Cosine.knnstructure")
  val eucMap = loadKNNStructure("data/100784_10_Euclidean.knnstructure")


  //TODO Remove this, but for now, it cleans broken KNN structures
  for(i <- cosMap.keysIterator) {
    if(!eucMap.contains(i)) {
      cosMap.remove(i)
    }
  }
  for(i <- eucMap.keysIterator) {
    if(!cosMap.contains(i)) {
      eucMap.remove(i)
    }
  }

  val queries = cosMap.keysIterator.take(qs)

  for(q <- queries) {
    this.globalSb.append("<div style=\"display:flexbox-inline\"><h2>Query Point:</h2></div><div style=\"display:flexbox-inline\">" + imgTag(q.toString) + "</div>")
    addSection(cosMap(q).sortBy(y => y._2).take(k).map(x => x._1), eucMap(q).sortBy(y => y._2).take(k).map(x => x._1))
    this.globalSb.append("\n\n")
  }

  outPut("data/compare.html")


  private def writeToFile(filepath:String, content:String) = {
    val file = new File(filepath)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(content)
    bw.close()
  }

  def addSection(a:Array[Int], b:Array[Int]): Unit = {
    this.globalSb.append("<div style=\"display:flexbox-inline\">")
    this.globalSb.append("<h4>Cosine:</h4>")
    addRow(a)
    this.globalSb.append("<h4>Eucledian:</h4>")
    addRow(b)
    this.globalSb.append("</div>")
  }

  def addRow(a:Array[Int]): Unit= {
    // Append whole result set for q'i
    this.globalSb.append("<div style=\"display:flexbox-inline\">")
    for(i <- a.indices) {
      this.globalSb.append(imgTag(a(i).toString))
    }
    this.globalSb.append("</div>")
  }

  def outPut(path:String) : Unit = {

    val sb = new StringBuilder
    // HEADER
    sb.append("<!doctype html>")
    sb.append("\n<html lang=\"en\">")
    sb.append("\n<head>")
    sb.append("\n<meta chartset=\"utf-8\">")
    sb.append("\n<title>ARKNNISS DEMO</title>")
    // triple double-quotes because of scala string interpolation bug
    sb.append("\n</head>")
    // BODY
    sb.append("\n<body>")

    sb.append("<div style=\"display:flexbox-inline\">")
    sb ++= this.globalSb
    sb.append("</div>")

    sb.append("\n</body>")
    sb.append("\n</html>")


    writeToFile(path, sb.mkString)
  }
  private def imgTag(id:String) = {
    val orgId = {
      val zeroC = 10 - id.length
      var zeroes = {
        val sb = new StringBuilder
        for(i <- 0 until zeroC) {
          sb.append("0")
        }
        sb
      }
      zeroes.append(id).toString
    }
    // triple double-quotes because of scala string interpolation bug
    s"""<img src=\"http://disa.fi.muni.cz/profimedia/images/$orgId\" />"""
  }

  def loadKNNStructure(path:String):mutable.HashMap[Int, Array[(Int, Float)]] = {
    println("Loading KNN Structure")
    val objReader = new ObjectInputStream(new FileInputStream(path))
    //KNNStructure
    val hashMap = objReader.readObject.asInstanceOf[mutable.HashMap[Int,Array[(Int,Float)]]]
    objReader.close()
    hashMap
  }
}
