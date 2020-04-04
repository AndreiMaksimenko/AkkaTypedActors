import akka.actor.Actor

class CounterActor extends Actor {
  import CounterActor._
  override def receive: Receive = counter(0, 0)
  def counter(valid: Int, invalid: Int): Receive = {
    case ValidPage   => context.become(counter(valid + 1, invalid))
    case InvalidPage => context.become(counter(valid, invalid + 1))
    case GetSucceed  => sender() ! valid
    case GetHandled  => sender() ! invalid
  }
}
object CounterActor {
  case object ValidPage
  case object InvalidPage
  case object GetSucceed
  case object GetHandled
}
