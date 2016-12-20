package speedtest
import utils.tools.Distance

case class SpeedConfig(
                              dataSetSize:Int,
                              queriesSetSize:Int,
                              functions:Int,
                              knn:Int,
                              tables:Int,
                              range:Double,
                              queries:String,
                              measure:Distance,
                              hashfunction:String,
                              numOfDim:Int,
                              buildFromFile:String,
                              probingScheme:String,
                              queriesWarmUp:String
                            )
