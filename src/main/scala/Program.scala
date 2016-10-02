import dataAnalysis.ClusterAnalysis

/**
  * Created by remeeh on 9/26/16.
  */

object Program {
  def main(args:Array[String]) = {
    val hello = new demo.Hello
    println(hello.sayHello("Hi from Main"))


    // Getting from file
    val data = IO.Parser.parseInput(getClass.getResource("descriptors-mini.data").getPath)

    // Outputting to file
    //IO.HTMLGenerator.outPut(data, "../", "index.html")

    // uncomment for distanalysis output
    //ClusterAnalysis.distAnalysis(data)

  }
}
