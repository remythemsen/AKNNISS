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
    //TODO Implement this.
    // open html document

    // convert tuple to image

    // close html document

    val content = "HELLOFILE"

    writeToFile(s"$path/$fileName", content)
  }

}
