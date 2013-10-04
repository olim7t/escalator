package olim7t

import akka.actor._
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import spray.routing.RequestContext
import spray.http.StatusCodes._

import Protocol._

trait ActorBackend extends Backend { self: Actor with ActorLogging =>
  implicit def executionContext: ExecutionContext

  implicit val timeout = Timeout(10 seconds)

  val elevator: ActorRef = context.actorOf(Props[Elevator])

  def handle(event: Event)(requestContext: RequestContext) {
    val responder = createResponder(requestContext)
    elevator.ask(event).pipeTo(responder)
  }

  def reportNextCommand(requestContext: RequestContext) {
    val responder = createResponder(requestContext)
    elevator.ask(NextCommand).pipeTo(responder)
  }

  private def createResponder(requestContext: RequestContext) =
    context.actorOf(Props(new Responder(requestContext, elevator)))
}
class Responder(requestContext: RequestContext, elevator: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case command: Command =>
      requestContext.complete(OK, command)
      self ! PoisonPill

    // Events are acknowledged even though we don't return a specific response.
    // We want to make sure that we don't return a command before the previous event was processed.
    case EventAck =>
      requestContext.complete(OK)
  }
}
