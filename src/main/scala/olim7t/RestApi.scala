package olim7t

import akka.actor._
import spray.routing._

import Protocol._

/** Abstract definition of a backend that will handle requests */
trait Backend {
  def handle(event: Event)(ctx: RequestContext)
  def reportNextCommand(ctx: RequestContext)
}

/** The HTTP interface that delegates to the backend */
trait RestApi extends HttpService { self: Backend =>
  def routes: Route = {

    val anyEvent = {
      import shapeless.{::, HNil}
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
      anyEvent { handle } ~
      path("nextCommand") { reportNextCommand }
    }
  }
}
