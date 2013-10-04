package olim7t

import spray.httpx.marshalling.Marshaller
import spray.http.{HttpEntity, ContentType}

object Protocol {
  sealed trait Event
  case class CallUp(from: Int) extends Event
  case class CallDown(from: Int) extends Event
  case class Go(target: Int) extends Event
  case object UserHasEntered extends Event
  case object UserHasExited extends Event
  case class Reset(cause: String) extends Event
  case object NextCommand extends Event

  sealed trait Response
  case object Nothing extends Response
  case object Up extends Response
  case object Down extends Response
  case object Open extends Response
  case object Close extends Response
  object Response {
    implicit val marshaller = Marshaller.of[Response](ContentType.`text/plain`) { (response, contentType, ctx) =>
      ctx.marshalTo(HttpEntity(response.toString.toUpperCase))
    }
  }
}

