import QuerySampleGenerator._

object Program extends App {
  val pathToFile="data/100k_direct_sample-norm.data"
  val inputSize =  100000 // 20172529
  val skipSize = 50000
  // val sampleSize = inputSize / skipSize
  val chunks = 1
  val dir = "data/"

  QuerySampleGenerator.generateQuerySampleFile(pathToFile, dir, skipSize, inputSize, chunks)

}
