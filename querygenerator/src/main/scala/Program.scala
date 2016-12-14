import QuerySampleGenerator._

object Program extends App {
  val pathToFile="./querygenerator/data/descriptors-decaf-reduced.data"
  val inputSize = 39286 /// get the real size
  val skipSize = 150
  val sampleSize = inputSize / skipSize
  val resultFileName="./querygenerator/data/query-"+sampleSize+".data"

  QuerySampleGenerator.generateQuerySampleFile(pathToFile, resultFileName, skipSize, inputSize)

}
