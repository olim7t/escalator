package olim7t

import org.scalatest.matchers.MustMatchers
import org.scalatest.WordSpec
import spray.http.HttpEntity
import spray.http.StatusCodes._
import spray.routing.RequestContext
import spray.testkit.ScalatestRouteTest

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

class RestApiSpec extends RestApi
                  with TestBackend
                  with WordSpec
                  with MustMatchers
                  with ScalatestRouteTest {

  def actorRefFactory = system

  "The REST API" must {
    "Delegate events to the backend" in {
      Get("/call?atFloor=0&to=UP") ~> routes ~> check {
        status must be === OK
        lastEvent must be === Some(Call(0, Up))
      }
      Get("/call?atFloor=2&to=DOWN") ~> routes ~> check {
        status must be === OK
        lastEvent must be === Some(Call(2, Down))
      }
      Get("/go?floorToGo=4") ~> routes ~> check {
        status must be === OK
        lastEvent must be === Some(Go(4))
      }
      Get("/userHasEntered") ~> routes ~> check {
        status must be === OK
        lastEvent must be === Some(UserHasEntered)
      }
      Get("/userHasExited") ~> routes ~> check {
        status must be === OK
        lastEvent must be === Some(UserHasExited)
      }
      Get("/reset?cause=Because+I+can") ~> routes ~> check {
        status must be === OK
        lastEvent must be === Some(Reset("Because I can"))
      }
    }

    "Respond with the next command" in {
      Get("/nextCommand") ~> routes ~> check {
        status must be === OK
        body must be === HttpEntity("NOTHING")
      }
    }
  }
}
