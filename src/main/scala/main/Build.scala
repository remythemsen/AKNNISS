package main

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Build {
  implicit val timeout = Timeout(3.hours)
  class Aggregator extends Actor {
    val c1 = context.actorOf(Props(new C), "C1")

    def receive = {
      case _ => {
        println("Hello from A")
        val r = c1 ? ""
        r.onComplete {
          case Success(x) => println("Success")
          case Failure(x) => println("Should come after C")
        }
      }
    }

    class C extends Actor {
      def receive = {
        case _ => {
          println("Hello from C")

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
