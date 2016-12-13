import QuerySampleGenerator._

object Program extends App {
  val pathToFile="data/descriptors-decaf-reduced.data"
  val inputSize = 20000000 /// get the real size
  val skipSize = 250
  val sampleSize = inputSize / skipSize
  val resultFileName="querysample-"+sampleSize+".data"

  QuerySampleGenerator.generateQuerySampleFile(pathToFile, resultFileName, skipSize, sampleSize, inputSize)

}
