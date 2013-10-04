package olim7t

import spray.http.StatusCodes._
import spray.routing.RequestContext
import Protocol._

trait TestBackend extends Backend {
  var lastEvent: Option[Event] = None

  def handle(event: Event)(ctx: RequestContext) {
    lastEvent = Some(event)
    ctx.complete(OK)
  }

  def reportNextCommand(ctx: RequestContext) {
    ctx.complete(OK, Nothing)
  }
}
