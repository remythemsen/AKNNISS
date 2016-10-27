package main

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Build {

  implicit val timeout = Timeout(3.hours)

  class Aggregator extends Actor {
    def receive = {
      case _ => {
        println("Starting A")

        // Remember the await stop when all has returned success,
        // or one has returned failure
        val futs = new ArrayBuffer[Future[Any]]()
        for(i <- 1 to 5) {
          val c = context.actorOf(Props(new C), "c"+i)
          futs += c ? ""
        }
        val res = Await.result(Future.sequence(futs), timeout.duration)
        println("all is finsished")
        println(res)
      }
    }

    class C extends Actor {
      def receive = {
        case _ => {
          println("running "+context.self.path)
          sender ! 5
        }
      }
    }
  }
  def main(args: Array[String]) = {
    val system = ActorSystem("LSHStructureBuilder")
    val sb = system.actorOf(Props(new Aggregator), "sb")
    sb ! "lol"
    system.terminate()
    println("lol")
  }
}
