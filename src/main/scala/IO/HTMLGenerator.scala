package IO
import java.io._

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

  def output(resultSet:List[(String,List[Double])], path:String, fileName:String) : Unit = {

    val sb = new StringBuilder
    // HEADER
    sb.append("<!doctype html>")
    sb.append("\n<html lang=\"en\">")
    sb.append("\n<head>")
    sb.append("\n<meta chartset=\"utf-8\">")
    sb.append("<title>The HTML5 Herald</title>")
    // triple double-quotes because of scala string interpolation bug
    sb.append(s"""<link rel=\"stylesheet\" href=\"$path/styles.css?v=1.0\">""")
    sb.append("</head>")
    // BODY
    sb.append("<body>")
    for(result <- resultSet) {
      sb.append(imgTag(result._1))
    }

    sb.append("</body>")
    sb.append("</html>")


    writeToFile(s"$path/$fileName", sb.mkString)
  }
  // triple double-quotes because of scala string interpolation bug
  private def imgTag(id:String) = s"""<img src=\"http://disa.fi.muni.cz/profimedia/images/$id\" />"""

}
