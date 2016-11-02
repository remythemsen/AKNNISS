/**
  * Created by remeeh on 9/27/16.
  * This object is meant to find precise k nearest neighbours and to calculate precision
  * on LSHStructures
  */

import java.io.File

import scala.io.Source
import IO.{HTMLGenerator, Parser}
import LSH.structures.LSHStructure
import breeze.linalg.DenseMatrix
import preProcessing.DimensionalityReducer
import tools.{Cosine, Distance}

import scala.collection.mutable.ArrayBuffer

object KNN {

  def main(args:Array[String]) = {
    println("Hello from KNN")
    val A:DenseMatrix[Double]= DimensionalityReducer.getRandMatrix(500,4096);
    val filename = "C:/Users/chm/Desktop/IdeaProjects/AKNNISS/data/descriptors-mini.data"
    val file = new File(filename)
    val data = new Parser(file)
    val next = data.next
    //println(data.vLength,data.size)

    val queryParser=new Parser(new File("C:/Users/chm/Desktop/IdeaProjects/AKNNISS/data/query.data"))
    val query=queryParser.next
    val queryR=(query._1,DimensionalityReducer.getNewVector(query._2,A))
    //println("query :"+query)

    //non reduced list
    var list:List[(String,Vector[Double])]=List()
    while(data.hasNext){
      list ::= data.next
    }

    //reduced List
    var reducedList:List[(String,Vector[Double])]=List()
    for(v<-list){
      reducedList ::= (v._1,DimensionalityReducer.getNewVector(v._2,A))
    }


    //for(x <-0 until 5){println(list(x))}

   var results = findKNearest(queryR,5,reducedList,Cosine)

//    for(r<-results){
//      println(r._1+" "+r._2)
//    }

    var list2=new ArrayBuffer[(String,Vector[Double])]()
    for(i<-results){
      for(j<- reducedList){
        if(i._1==j._1){
          list2+=j
        }
      }
    }

//    for(l<-list2){
//      println(l._1+" "+l._2)
//    }

    val outdir="C:/Users/chm/Desktop/IdeaProjects/AKNNISS/out/results/KNN3"
    HTMLGenerator.outPut(list2,queryR,outdir)

  }
  def findKNearest(q:(String,Vector[Double]), k:Int, tuples:List[(String, Vector[Double])], distance:Distance) : List[(String, Double)] = {
    // Measure distances from q to each other tuple
    val distances = for {
      t <- tuples
    } yield (t._1, distance.measure(q._2, t._2))

    // Take out first k, return candidate-set
    distances.sortBy(_._2).take(k)
  }

  def precision(structure:LSHStructure) = {
    // Run queries on structure, then compare results with
    // results from KNN
  }



}
