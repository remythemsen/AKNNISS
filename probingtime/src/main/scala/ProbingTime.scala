import java.io.File
import java.nio.file.{Files, Paths, StandardOpenOption}
import java.util

import LSH.hashFunctions.{CrossPolytope, Hyperplane}
import LSH.multiprobing.{MultiProbingCrossPolytope, MultiProbingHyperplane}
import utils.IO.ReducedFileParser
import utils.tools.Timer

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/**
  * Created by christo on 24-Dec-16.
  */
object ProbingTime {

  val pConfig=new probeConfigParser("data/probeconfig.txt")
  val rnd=new Random(System.currentTimeMillis())
  var bufferProbeTime=new ArrayBuffer[Float]()
  var totalProbeTime:Float=0.0f
  val timer=new Timer

  def main(args: Array[String]): Unit = {
    while(pConfig.hasNext){
      val config=pConfig.next
      val data = new ReducedFileParser(new File(config.fileName))
      val warmup=new ReducedFileParser(new File(config.warmupfile))
      val function = config.hashfunction match{
        case "Hyperplane" => new Hyperplane(config.functions, () => new Random(rnd.nextLong),config.numOfDim)
        case "Crosspolytope" => new CrossPolytope(config.functions, () => new Random(rnd.nextLong),config.numOfDim)
      }
      println(config.hashfunction)
      //DO the warm up before running probing time
      while(warmup.hasNext){
        var p=warmup.next._2
        var hcode=function.apply(p)
        config.probingScheme match {
          case "Hyperplane" =>
            val res = new MultiProbingHyperplane(function.apply(p))
            var bucketsToBeProbed = res.generateProbes.map(x => util.Arrays.hashCode(x))

          case "Crosspolytope" =>
            // T = 3
            val rotations = function.asInstanceOf[CrossPolytope].rotations
            val arrayOfMaxIndices = function.asInstanceOf[CrossPolytope].arrayOfMaxIndices
            val res = new MultiProbingCrossPolytope(rotations, arrayOfMaxIndices, config.numOfProbes)
            var  bucketsToBeProbed = res.generateProbes.map(x => util.Arrays.hashCode(x))
        }
      }//warm up end

      //compute probe time
      while(data.hasNext){
        var point=data.next._2
        var hashCode=function.apply(point)
        config.probingScheme match {
          case "Hyperplane" =>
            val res = new MultiProbingHyperplane(function(point))

            timer.play()
            var bucketsToBeProbed = res.generateProbes.map(x => util.Arrays.hashCode(x))
            totalProbeTime+=timer.check().toFloat
            println(totalProbeTime)
            bufferProbeTime+=totalProbeTime

          case "Crosspolytope" =>
            // T = 3
            val rotations = function.asInstanceOf[CrossPolytope].rotations
            val arrayOfMaxIndices = function.asInstanceOf[CrossPolytope].arrayOfMaxIndices
            val res = new MultiProbingCrossPolytope(rotations, arrayOfMaxIndices, config.numOfProbes)

            timer.play()
            var  bucketsToBeProbed = res.generateProbes.map(x => util.Arrays.hashCode(x))
            totalProbeTime+=timer.check().toFloat
            println(totalProbeTime)
            bufferProbeTime+=totalProbeTime
        }

      }

      var mean:Float=totalProbeTime/data.size
      val Variance = {
        var tmp = 0f
        for (r <- bufferProbeTime) {
          tmp += (r - mean) * (r - mean)
        }
        tmp /bufferProbeTime.size
      }
      val timeStdDev = Math.sqrt(Variance).toFloat

      val sb = new StringBuilder
      sb.append(config.dataSetSize + " ")
      sb.append(config.functions + " ")
      sb.append(config.numOfDim + " ")
      sb.append(config.hashfunction + " ")
      sb.append(config.probingScheme + " ")
      sb.append(config.numOfProbes + " ")
      sb.append(((totalProbeTime )/ data.size)*1000+" ms" + " ")
      sb.append(timeStdDev + " ")
      sb.append(System.getProperty("line.separator"))
      // Write resulting set
      Files.write(Paths.get("data/probeTime.log"), sb.toString.getBytes(), StandardOpenOption.APPEND)

      // Reset
      this.bufferProbeTime = ArrayBuffer.empty
      totalProbeTime=0.0f

    }

  }
}
