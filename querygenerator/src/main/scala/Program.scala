import QuerySampleGenerator._

object Program extends App {
  val pathToFile="data/descriptors-decaf-reduced.data"
  val inputSize = 39286//20172529 /// get the real size
  val skipSize = 400//500
  val sampleSize = inputSize / skipSize
  val chunks = 1
  val dir = "data/"

  QuerySampleGenerator.generateQuerySampleFile(pathToFile, dir, skipSize, inputSize, chunks)

}
