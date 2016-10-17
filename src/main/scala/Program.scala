import java.io.{File, FileWriter}
import LSH.structures.LSHStructure
import LSH.hashFunctions.Hyperplane
import tools.Cosine

/**
  * Created by remeeh on 9/26/16.
  */
case class Config(foo: Int = -1, out: File = new File("."), xyz: Boolean = false,
  libName: String = "", maxCount: Int = -1, verbose: Boolean = false, debug: Boolean = false,
  mode: String = "", files: Seq[File] = Seq(), keepalive: Boolean = false,
  jars: Seq[File] = Seq(), kwargs: Map[String,String] = Map())

object Program {
  def main(args:Array[String]) = {

    // Getting from file
/*    val data = IO.Parser.parseInput(getClass.getResource("descriptors-mini.data").getPath)
    val lshs = new LSHStructure(data, () => new Hyperplane(12), 4)
    val result = lshs.query(data.head, 10, 10.0, Cosine)

    // Outputting to file
    IO.HTMLGenerator.outPut(result)*/


    val parser = new scopt.OptionParser[Unit]("scopt") {
      head("scopt", "3.x")
    }
    if (parser.parse(args)) {
      // do stuff
    }
    else {
      // arguments are bad, usage message will have been displayed
    }



  }

}
