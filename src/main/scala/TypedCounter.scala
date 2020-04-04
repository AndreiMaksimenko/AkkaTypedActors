import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }

object TypedCounter {
  sealed trait Command
  case object ValidPage extends Command
  case object InvalidPage extends Command
  case class GetSucceed(sender: ActorRef[CountValid]) extends Command
  case class GetHandled(sender: ActorRef[CountInvalid]) extends Command
  case class CountValid(valid: Int)
  case class CountInvalid(invalid: Int)

  def apply(valid: Int = 0, invalid: Int = 0): Behavior[Command] = Behaviors.receiveMessage {
    case ValidPage =>
      TypedCounter(valid + 1, invalid)
    case InvalidPage =>
      TypedCounter(valid, invalid + 1)
    case GetSucceed(sender) =>
      sender ! CountValid(valid)
      Behaviors.same
    case GetHandled(sender) =>
      sender ! CountInvalid(invalid)
      Behaviors.same
  }
}
