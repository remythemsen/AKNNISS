package tools.status

/**
  * Created by remeeh on 03-12-2016.
  */
trait Status {
}
case object Ready extends Status
case class InProgress(progress:Int) extends Status
case object NotReady extends Status
