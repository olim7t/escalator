package olim7t

import akka.actor.ActorLogging
import spray.routing.RequestContext
import spray.http.StatusCodes._

import Protocol._

trait ActorBackend extends Backend { self: ActorLogging =>
  def handle(event: Event)(ctx: RequestContext) {
    log.debug(s"Handling ${event}")
    ctx.complete(OK)
  }

  def reportNextCommand(ctx: RequestContext) {
    val command = Nothing

    log.debug(s"Returning next command ${command}")
    ctx.complete(OK, command)
  }
}
