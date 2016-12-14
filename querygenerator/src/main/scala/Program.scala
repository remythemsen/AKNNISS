import QuerySampleGenerator._

object Program extends App {
  val pathToFile="./querygenerator/data/descriptors-decaf-reduced.data"
  val inputSize = 39286 /// get the real size
  val skipSize = 500
  val sampleSize = inputSize / skipSize
  val chunks = 5
  val dir = "./querygenerator/data/"

  QuerySampleGenerator.generateQuerySampleFile(pathToFile, dir, skipSize, inputSize, chunks)

}
