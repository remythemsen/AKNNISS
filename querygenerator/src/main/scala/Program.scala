import QuerySampleGenerator._

object Program extends App {
  val pathToFile="data/descriptors-decaf-1m.data"
  val inputSize = 20172529 //  10000
  val skipSize = 122
  // val sampleSize = inputSize / skipSize
  val chunks = 1
  val dir = "data/"

  QuerySampleGenerator.generateQuerySampleFile(pathToFile, dir, skipSize, inputSize, chunks)

}
