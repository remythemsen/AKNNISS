import QuerySampleGenerator._

object Program extends App {
  val pathToFile="data/descriptors-decaf-1m.data"
  val inputSize =  1008935 // 20172529
  val skipSize = 600
  // val sampleSize = inputSize / skipSize
  val chunks = 5
  val dir = "data/"

  QuerySampleGenerator.generateQuerySampleFile(pathToFile, dir, skipSize, inputSize, chunks)

}
