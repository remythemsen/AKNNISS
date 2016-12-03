import akka.actor._

object Program  extends App {
  val system = ActorSystem("PerformanceTesterSystem")
  val localActor = system.actorOf(Props[PerformanceTester], name = "PerformanceTester")  // the local actor
  localActor ! "START"                                                     // start the action
}

class PerformanceTester extends Actor {

  // create the remote actor
  val remote = context.actorSelection("akka.tcp://TableBuilderSystem@172.19.0.2:2552/user/TableBuilder")

  def receive = {
    case "START" =>
      remote ! "Hi there tablebuilder"
    case msg: String =>
      println(s"ive gotten a message from tablebuilder: '$msg'")
  }
}

