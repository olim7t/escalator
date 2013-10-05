package olim7t

import spray.httpx.marshalling.Marshaller
import spray.http.{HttpEntity, ContentType}

object Protocol {
  sealed trait Request

  sealed trait Event extends Request
  case class Call(from: Int, direction: Direction) extends Event
  case class Go(target: Int) extends Event
  case object UserHasEntered extends Event
  case object UserHasExited extends Event
  case class Reset(cause: String) extends Event

  case object NextCommand extends Request


  sealed trait Command
  sealed trait DoorStatus extends Command
  case object Open extends DoorStatus
  case object Close extends DoorStatus
  sealed trait Direction extends Command
  case object Up extends Direction
  case object Down extends Direction
  case object Nothing extends Command

  object Command {
    implicit val marshaller = Marshaller.of[Command](ContentType.`text/plain`) { (command, contentType, ctx) =>
      ctx.marshalTo(HttpEntity(command.toString.toUpperCase))
    }
  }

  case object EventAck
}

