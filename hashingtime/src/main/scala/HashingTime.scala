import java.io.File
import java.nio.file.{Files, Paths, StandardOpenOption}
import LSH.hashFunctions.{CrossPolytope, Hyperplane}
import utils.IO.ReducedFileParser
import utils.tools.Timer
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/**
  * Created by christo on 24-Dec-16.
  */
object HashingTime {

  val htConfig=new hConfigParser("data/htconfig.txt")
  val rnd=new Random(System.currentTimeMillis())
  var bufferHashTime=new ArrayBuffer[Float]()
  var totalHashTime:Float=0.0f
  val timer=new Timer

  def main(args: Array[String]): Unit = {
    while(htConfig.hasNext){
      val config=htConfig.next
      val data = new ReducedFileParser(new File(config.fileName))
      val warmup=new ReducedFileParser(new File(config.warmupfile))
      val function = config.hashfunction match{
        case "Hyperplane" => new Hyperplane(config.functions, () => new Random(rnd.nextLong),config.numOfDim)
        case "Crosspolytope" => new CrossPolytope(config.functions, () => new Random(rnd.nextLong),config.numOfDim)
      }
      println(config.hashfunction)
      //DO the warm up before running hashing time
      while(warmup.hasNext){
        var p=warmup.next._2
        var hcode=function.apply(p)
      }//warm up end

      //compute query time
      while(data.hasNext){
        var point=data.next._2
        timer.play()
        var hashCode=function.apply(point)
        totalHashTime+=timer.check().toFloat
        println(totalHashTime)
        bufferHashTime+=totalHashTime
      }

      var mean:Float=totalHashTime/data.size
      val Variance = {
        var tmp = 0f
        for (r <- bufferHashTime) {
          tmp += (r - mean) * (r - mean)
        }
        tmp /bufferHashTime.size
      }
      val timeStdDev = Math.sqrt(Variance).toFloat

      val sb = new StringBuilder
      sb.append(config.dataSetSize + " ")
      sb.append(config.functions + " ")
      sb.append(config.numOfDim + " ")
      sb.append(config.hashfunction + " ")
      sb.append(((totalHashTime )/ data.size)*1000+" ms" + " ")
      sb.append(timeStdDev + " ")
      sb.append(System.getProperty("line.separator"))
      // Write resulting set
      Files.write(Paths.get("data/hashQueryTime.log"), sb.toString.getBytes(), StandardOpenOption.APPEND)

      // Reset
      this.bufferHashTime = ArrayBuffer.empty
      totalHashTime=0.0f

    }

  }
}
