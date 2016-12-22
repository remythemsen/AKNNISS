/**
  * Created by Roxana on 12/8/16.
  */

package reducer

import java.io._
import java.util.concurrent.ArrayBlockingQueue
import utils.IO.DisaFileParser
import utils.tools.Distance
import breeze.stats.distributions.Gaussian
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.math.{pow, sqrt}
import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors

case class Config(data:File, outDir:String)

object Program extends App {

  implicit val ec = ExecutionContext.fromExecutorService(Executors.newWorkStealingPool(8))

  val config = new Config(new File("reducer/data/descriptors-decaf-random-sample.data"), "./tablehandler/data")
  println("Generating Random Matrix")

  // TODO Find replacement of random
  val rnd = new Gaussian(0,1)

  // Random Matrix containing values from 0-1 (256 x 4096)
  val randomMatrix = DimensionalityReducer.getRandMatrix(20000000, 4096, rnd)
  // val randomMatrix: DenseMatrix[Float] = DimensionalityReducer.getRandMatrix(20000000, 4096);
  // Matrix of precomputed Vectors to build reduced vectors from (256 x 4096)



  var n = 0
  val p = 4
  // Number of threads // Same time no matter what number from 4-16
  val loadedTuples = new ArrayBlockingQueue[(Int, Array[Float])](10000)
  val preProcessedTuples = new ArrayBlockingQueue[(Int, Array[Float])](20)

  val input = new DisaFileParser(config.data)
  n = input.size
  var progress = 0
  println(n)

  Future {
    while (input.hasNext) {
      loadedTuples.put(input.next)

    }
  }.onFailure {
    case t => println("An error has occured in the input parser: " + t.getMessage)
  }

  for (i <- 0 until p) {
    Future {
      while (true) {
        var tuple = loadedTuples.take()
        val aux = new Array[Float](256)
        DimensionalityReducer.getNewVector(tuple._2, randomMatrix, aux)
        val reducedTuple = (tuple._1, aux)
        preProcessedTuples.put(reducedTuple)
      }
    }.onFailure {
      case t => println("An error has occured: " + t.getMessage)
    }
  }

  val dir: String = config.outDir.concat("/")
    // constructing filename
    .concat(config.data.getName.substring(0, config.data.getName.length - 5))
    .concat("-reduced.data")

  val output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dir.toString)))

  var j = 0.0

  while (progress != n) {

    var t = preProcessedTuples.take()
    if(t._2.length != 256) {
      println(t._2.length)
    }
    var sb = new StringBuffer(456)
    sb.append(t._1)
    sb.append(" ")
    for (component <- t._2) {
      sb.append(component + " ")
    }
    sb.append("\n")

    // Write resulting set
    output.write(sb.toString())

    j += 1.0
    progress += 1
    if (j % 100 == 0) {
      println(((j / n) * 100).toInt.toString + "%")
    }
  }
  println("Finished with "+n+" tuples")
}

object DimensionalityReducer{

  def getNewVector(x:Array[Float], matrix:Array[Array[Float]], a: => Array[Float]) = {
    MatrixVectorProduct(x,matrix,a)//return Reduced Vector
  }

  def getRandMatrix(n:Int, d:Int, rnd:Gaussian): Array[Array[Float]] ={

    val epsilon=1// 0 <= epsilon <= 1
    val base2 = scala.math.log(2)
    val log2N = scala.math.log(n) / base2
    // m = new reduced dimension
    val m=((9*epsilon*log2N).toInt) + 38

    val randomMatrix = for {
      i <- (0 until m).toArray
      b <- Array(new Array[Float](d))
    } yield b

    // Populate bMatrix
    for (i <- 0 until m) {
      for (j <- 0 until d) {
        // dimenstions in each Vector
        randomMatrix(i)(j) = rnd.sample.toFloat
      }
    }
    val M=normalizeMatrix(randomMatrix)
    M
  }

  def MatrixVectorProduct(x:Array[Float],matrix:Array[Array[Float]], a: => Array[Float])={
    // TODO BUild while init'ing
    for (i <- 0 until 256) {
      a(i) = Distance.parallelDotProduct(x, matrix(i))
    }
  }

  def normalizeMatrix(A:Array[Array[Float]]):Array[Array[Float]]={
    val buffer= new ArrayBuffer[Float]

    val B = for {
      i <- (0 until A.length).toArray
      b <- Array(new Array[Float](A(0).length))
    } yield b

    // Populate bMatrix
    for (i <- 0 until A.length) {
      for (j <- 0 until A(0).length) {
        // dimenstions in each Vector
        B(i)(j) = 0f
      }
    }
    for(i<-0 until A.length){
      val b = new ArrayBuffer[Float]
      for(j<-0 until A(0).length){
        b+=A(i)(j) // Note this conversion
      }
      val l= sqrt(b.map { case (x) => pow(x, 2) }.sum)
      for(c <-0 until A(0).length){
        B(i)(c) = (b(c)/l).toFloat
      }
    }
    B
  }
}

