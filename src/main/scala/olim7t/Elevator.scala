package olim7t

import akka.actor.{Actor, ActorLogging}

import Protocol._

class Elevator extends Actor with ActorLogging with ElevatorController {
  def receive = {
    case event: Event =>
      log.debug(s"Handling ${event} - state before: ${dumpState}")
      handle(event)
      log.debug(s"Handling ${event} - state after:  ${dumpState}")
      sender ! EventAck

    case NextCommand =>
      log.debug(s"Returning command - state before: ${dumpState}")
      val command = nextCommand
      log.debug(s"Returning ${command} - state after: ${dumpState}")
      sender ! command
  }
}
