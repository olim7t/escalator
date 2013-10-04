package olim7t

import akka.actor.{Actor, ActorLogging}

import Protocol._

class Elevator extends Actor with ActorLogging {
  def receive = {
    case event: Event =>
      log.debug(s"Handling ${event}")
      sender ! EventAck

    case NextCommand =>
      val command = Nothing
      log.debug(s"Returning next command ${command}")
      sender ! command
  }
}
