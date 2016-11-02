package IO
import java.io._

import tools.{Cosine, Distance}

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

  def outPut(resultSet:ArrayBuffer[(String,Vector[Double])], queryPoint:(String,Vector[Double]), path:String) : Unit = {
    val sb = new StringBuilder
    // HEADER
    sb.append("<!doctype html>")
    sb.append("\n<html lang=\"en\">")
    sb.append("\n<head>")
    sb.append("\n<meta chartset=\"utf-8\">")
    sb.append("\n<title>ARKNNISS DEMO</title>")
    sb.append("<style>div {float:left;width:100%;margin-bottom:10px;background-color:#dddddd;}img{float:left;width:130px;margin-right:15px;}</style>")
    // triple double-quotes because of scala string interpolation bug
    sb.append("\n</head>")
    // BODY
    sb.append("\n<body>")
    sb.append("<div>")
    sb.append(imgTag(queryPoint._1))
    sb.append("<p><b>Query Point</b></p>")
    sb.append("</div>")
    val sortedRes = resultSet.zip(resultSet.map(x => (Cosine.measure(queryPoint._2, x._2)))).sortBy(_._2)
    for(result <- sortedRes) {
      sb.append("<div>")
      sb.append(imgTag(result._1._1))
      sb.append("<p>Tuple ID: "+ result._1._1 +"</p>")
      // TODO make dist measure selec dynamic
      sb.append("<p>Dist from q: "+ result._2 +"</p>")
      sb.append("</div>")
    }

    sb.append("\n</body>")
    sb.append("\n</html>")


    writeToFile(path, sb.mkString)
  }
  // triple double-quotes because of scala string interpolation bug
  private def imgTag(id:String) = s"""<img src=\"http://disa.fi.muni.cz/profimedia/images/$id\" />"""

}
