import tools.actorMessages._
import tools.status._
import akka.actor._
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import akka.actor.{ActorSystem, Props}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

object Program  extends App {
  val system = ActorSystem("PerformanceTesterSystem")
  val localActor = system.actorOf(Props[PerformanceTester], name = "PerformanceTester")  // the local actor
  localActor ! "START"                                                     // start the action
}

class PerformanceTester extends Actor {

  val t1 = context.actorSelection("akka.tcp://TableBuilderSystem@172.19.0.2:2552/user/TableBuilder")
  val t2 = context.actorSelection("akka.tcp://TableBuilderSystem@172.19.0.3:2552/user/TableBuilder")
  val structure = List(t1, t2)

  def receive = {
    case "START" => {
      val rnd = new Random()
      t1 ! FillTable("Hyperplane", 10, rnd.nextLong())
      t2 ! FillTable("Hyperplane", 10, rnd.nextLong())
      var condition = false
      implicit val timeout = Timeout(1.hour)
      while(!condition) {
        val statuses = {
          Await.result(Future.sequence {
            for {
              actor <- structure
              status <- {
                val s = (actor ? GetStatus).asInstanceOf[Future[Status]]
                List(s)
              }
            } yield status.asInstanceOf[Future[Status]]
          }, Timeout(1.minute).duration)
        }

        condition = true
        for (s <- statuses) {
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
    }
    case NotReady =>
      println("It's not ready")
    case InProgress(x) => {
      println("it's in progresss" + x)
    }
  }
}

