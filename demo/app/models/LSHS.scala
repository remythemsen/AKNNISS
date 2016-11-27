package models

import java.io.File

import LSH.actors._
import akka.actor.Status.Status
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import utils.IO.Parser
import utils.tools.Cosine

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Random

class LSHS {
  val functions = 11
  val tables = 3
  val data = "data/test/descriptors-decaf-random-sample-reduced.data"
  val rnd = new Random(System.currentTimeMillis())
  val hf = () => new LSH.hashFunctions.Hyperplane(functions, () => new Random(rnd.nextLong()))
  val range = 1.0
  val dist = Cosine

  // Initializing the tables...
  val system = ActorSystem("LSHStructureBuilder")
  implicit val timeout = Timeout(1.hour)

  // Building the structure
  val lshStructure =
      for {
        i <- 0 until tables
        parser <- List(new Parser(new File(data)))
        actor <- {
          implicit val timeout = Timeout(5.hours)
          val a = system.actorOf(Props(new LSH.actors.TableActor(hf)))
          a ! FillTable(parser)
          List(a)
        }
      } yield actor

  var condition = false

  while(!condition) {
    val statuses:IndexedSeq[Status] = {
      Await.result(Future.sequence {
        for {
          actor <- lshStructure
          status <- {
            val s = (actor ? GetStatus).asInstanceOf[Future[Status]]
            List(s)
          }
        } yield status.asInstanceOf[Future[Status]]
      }, Timeout(1.minute).duration)
    }

    condition = true
    for(s <- statuses) {
      val msg = s match {
        case NotReady => {
          condition = false
          "Not Initialized"
        }
        case Ready => "Ready"
        case InProgress(p) => {
          condition = false
          p.toString + "% Done"
        }
      }
      print(msg + "\t")
    }
    println("")
    Thread.sleep(200)
  }

  // Make lookup table
  val parser = new utils.IO.Parser(new File(data))
  val lookupMap:Map[String, Array[Float]] = {
    for {
      i <- 0 until parser.size
      r <- {
        val t = parser.next
        List(t._1 -> t._2)
      }
    } yield r
  }.toMap

  println("All is done!")

  def findVectorById(id:String):Array[Float] = {
    lookupMap.get(id).head
  }

  def getResults(id:String):(String, List[((String, Array[Float]), Float)]) = {
    val fVector = findVectorById(id)

    val tres = time {
      getCandidateSet(fVector, range)
    }

    val res2 = tres._1.zip(tres._1.map(x => Cosine.measure(fVector, x._2))).sortBy(_._2)
    (tres._2, res2.toList)
  }

  def getCandidateSet(fVector:Array[Float], range:Double) : IndexedSeq[(String, Array[Float])] = {
    val candidates = {
      Await.result(Future.sequence {
        for {
          actor <- lshStructure
          queries <- {
            val r = (actor ? actors.Query(fVector)).asInstanceOf[Future[Either[String, QueryResult]]]
            List(r)
          }
        } yield queries.asInstanceOf[Future[Either[String, QueryResult]]]
      }, Timeout(1.minute).duration)
    }.flatMap {
      case Right(v) => v.items
      case Left(s) => IndexedSeq.empty
    }.distinct.filter(x => dist.measure(x._2, fVector) < range)

    candidates
  }

  def time[R](block: => R) = {
    val t0 = System.currentTimeMillis()
    val result = block    // call-by-name
    val t1 = System.currentTimeMillis()
    (result, (t1 - t0) + "ms")
  }
}
