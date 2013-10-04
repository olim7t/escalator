package olim7t

import Protocol._

import akka.actor._

import spray.routing._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing.RequestContext
import akka.util.Timeout
import scala.concurrent.duration._
import spray.httpx.unmarshalling.Unmarshaller
import spray.http.HttpEntity
import shapeless.HNil

class RestInterface extends Actor
                    with HttpServiceActor
                    with RestApi {
  def receive = runRoute(routes)
}

trait RestApi extends HttpService with ActorLogging { actor: Actor =>
  implicit val timeout = Timeout(10 seconds)
  import akka.pattern.ask
  import akka.pattern.pipe

  def logAndComplete(request: Event) = complete {
    log.debug(s"Got request ${request}")
    OK
  }

  def routes: Route = {
    // factor all event-handling routes, so that we can write a generic handler for them later
    val eventRoutes = {
      import shapeless.::
      type EventRoute = Directive[Event :: HNil]

      val callUp: EventRoute = (path("call") & parameters('atFloor, 'to ! "UP")).as(CallUp)
      val callDown: EventRoute = (path("call") & parameters('atFloor, 'to ! "DOWN")).as(CallDown)
      val go: EventRoute = (path("go") & parameters('floorToGo)).as(Go)
      val entered: EventRoute = path("userHasEntered") & provide(UserHasEntered: Event)
      val exited: EventRoute =  path("userHasExited") & provide(UserHasExited: Event)
      val reset: EventRoute = (path("reset") & parameters('cause)).as(Reset)

      callUp | callDown | go | entered | exited | reset
    }

    get {
      eventRoutes { logAndComplete } ~
      path("nextCommand") {
        complete {
          log.debug(s"Returning next command")
          Nothing
        }
      }
    }
  }

//  def createResponder(requestContext:RequestContext) = {
//    context.actorOf(Props(new Responder(requestContext, boxOffice)))
//  }

}

class Responder(requestContext:RequestContext, ticketMaster:ActorRef) extends Actor with ActorLogging {
  import spray.httpx.SprayJsonSupport._

  def receive = {
    case _ =>
      // TODO handle replies from backend actor
      self ! PoisonPill

//    case ticket:Ticket =>
//      requestContext.complete(StatusCodes.OK, ticket)
//      self ! PoisonPill
//
//    case EventCreated =>
//      requestContext.complete(StatusCodes.OK)
//      self ! PoisonPill
//
//    case SoldOut =>
//      requestContext.complete(StatusCodes.NotFound)
//      self ! PoisonPill
//
//    case Events(events) =>
//      requestContext.complete(StatusCodes.OK, events)
//      self ! PoisonPill

  }
}