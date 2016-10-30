package IO
import java.io._

import scala.collection.mutable.ArrayBuffer

/**
  * Created by remeeh on 9/26/16.
  */
object HTMLGenerator {
  private def writeToFile(filepath:String, content:String) = {
    val file = new File(filepath)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(content)
    bw.close()
  }

  def outPut(resultSet:ArrayBuffer[(String,Vector[Float])], path:String) : Unit = {

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
    for(result <- resultSet) {
      sb.append(imgTag(result._1))
    }

    sb.append("\n</body>")
    sb.append("\n</html>")


    writeToFile(path, sb.mkString)
  }
  // triple double-quotes because of scala string interpolation bug
  private def imgTag(id:String) = s"""<img src=\"http://disa.fi.muni.cz/profimedia/images/$id\" />"""

}
