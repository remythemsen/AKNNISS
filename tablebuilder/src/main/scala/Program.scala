import akka.actor._

object TableBuilder extends App {
  val system = ActorSystem("TableBuilderSystem")
  val remoteActor = system.actorOf(Props[TableBuilder], name = "TableBuilder")
  remoteActor ! "A Tablebuilder came alive"
}

class TableBuilder extends Actor {
  def receive = {
    case msg: String =>
      println(s"TableBuilder received message '$msg'")
      sender ! "thanks for the message"
  }
}

